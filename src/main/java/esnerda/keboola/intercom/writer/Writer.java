/*
 */
package esnerda.keboola.intercom.writer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.supercsv.exception.SuperCsvException;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import esnerda.keboola.components.KBCException;
import esnerda.keboola.components.configuration.handler.ConfigHandlerBuilder;
import esnerda.keboola.components.configuration.handler.KBCConfigurationEnvHandler;
import esnerda.keboola.components.configuration.tableconfig.ManifestFile;
import esnerda.keboola.components.logging.DefaultLogger;
import esnerda.keboola.components.logging.KBCLogger;
import esnerda.keboola.intercom.writer.client.Client;
import esnerda.keboola.intercom.writer.client.ClientException;
import esnerda.keboola.intercom.writer.client.FailedItemsCollection;
import esnerda.keboola.intercom.writer.client.IntercomValidationException;
import esnerda.keboola.intercom.writer.client.request.UserBulkJobRequest;
import esnerda.keboola.intercom.writer.client.request.UserObjectBuilder;
import esnerda.keboola.intercom.writer.config.IntercomWrLastState;
import esnerda.keboola.intercom.writer.config.IntercomWrParameters;
import esnerda.keboola.intercom.writer.config.UserMapping;

/**
 *
 * @author David Esner <esnerda at gmail.com>
 * @created 2016
 */
public class Writer {

    //max run time before give up result collection (Seconds)
    private final static long MAX_RUN_TIME = 19800;
    //milis between jobStatus refresh request
    private final static long REQ_WAIT_INTERVAL = 1000;

    static KBCLogger logger = new DefaultLogger(Writer.class);

    public static void main(String[] args) {

        startCounter();

        if (args.length == 0) {
            System.out.print("No parameters provided.");
            System.exit(1);
        }
        //tt
        IntercomWrParameters confParams = null;
        ICsvMapReader csvreader = null;
        UserMapping userMapping = null;
        KBCConfigurationEnvHandler handler = null;
        List<String> jobIds = new ArrayList();
        FailedItemsCollection failedJobs = new FailedItemsCollection("User");
        File usersInput = null;

        try {
            handler = ConfigHandlerBuilder.create(IntercomWrParameters.class)
                    .hasInputTables(true)
                    .setStateFileType(IntercomWrLastState.class)
                    .build();

            //process the configuration
            handler.processConfigFile(new File(args[0]));
            confParams = (IntercomWrParameters) handler.getParameters();
            usersInput = handler.getInputTables().get(0).getCsvTable();
        } catch (KBCException ex) {
            logger.log(ex);
            System.err.print(ex.getDetailedMessage());
            System.exit(1);
        }
        int unproccessedJobsCount = 0;
        try {
            Client client = new Client(confParams.getAppId(), confParams.getApiKey());

            /*Get last state file*/
            IntercomWrLastState lastState = (IntercomWrLastState) handler.getStateFile();
            if (lastState == null || lastState.getUnfinishedUserJobIds() == null) {
                logger.info("Last state file does not exist. First run?");
            } else {
                /*Collect results from last run*/
                logger.info("Collecting results from the last run. Total number: " + lastState.getUnfinishedUserJobIds().size());
                jobIds.addAll(lastState.getUnfinishedUserJobIds());
                try {
                    failedJobs.addAll(
                            client.waitAndCollectResults(jobIds, 240, 0)
                    );
                } catch (ClientException ex) {
                    logger.warning(ex.getDetailedMessage());
                } catch (FailedItemsCollection.IllegalValueException ex) {
                    logger.error(ex.getMessage());
                }
                if (jobIds.isEmpty()) {
                    logger.info("Collecting results finished. All " + lastState.getUnfinishedUserJobIds().size() + "jobs from last run collected. With " + failedJobs.getItems().size() + " errors.");
                } else {
                    logger.warning("Collecting results finished. " + jobIds.size() + " jobs from last run were not collected. Remaining finished with " + failedJobs.getItems().size() + " errors.");
                }

            }
            //how many jobs still not finished?
            unproccessedJobsCount = jobIds.size();
            /*Proccess input table*/
            csvreader = new CsvMapReader(new FileReader(usersInput), CsvPreference.STANDARD_PREFERENCE);
            // get header
            final String[] header = csvreader.getHeader(true);

            /*validate mapping*/
            userMapping = confParams.getUserColMapping();
            userMapping.validateHeaderMapping(header);

            /*Send data*/
            logger.info("Submiting User bulk jobs.");
            UserBulkJobRequest userReq = new UserBulkJobRequest();
            Map<String, String> userData = new HashMap();

            while ((userData = csvreader.read(header)) != null) {
                try {
                    if (!userReq.isFull()) {
                        //insert user item to a request
                        userReq.addPostItem(
                                new UserObjectBuilder(userData, userMapping.getStandardColumnMapping())
                                .setUserCustomColumns(userData, userMapping.getCustomColumns())
                                .setCompany(userData, userMapping.getCompanyColumns().getStandardColumnMapping(),
                                        userMapping.getCompanyColumns().getCustomColumns())
                                .build());
                    } else {
                        try {
                            //submit request if full
                            String jobId = client.submitUserBulkJobRequest(userReq);
                            //collect job Id
                            jobIds.add(jobId);
                            //reset request
                            userReq = new UserBulkJobRequest();
                            //add current data
                            userReq.addPostItem(
                                    new UserObjectBuilder(userData, userMapping.getStandardColumnMapping())
                                    .setUserCustomColumns(userData, userMapping.getCustomColumns())
                                    .setCompany(userData, userMapping.getCompanyColumns().getStandardColumnMapping(),
                                            userMapping.getCompanyColumns().getCustomColumns())
                                    .build());
                        } catch (ClientException ex) {
                            if (ex.getSeverity() < 2) {
                                logger.log(ex.getSeverity(), ex.getMessage() + ex.getDetailedMessage());
                                try {
                                    //collect failed jobs
                                    failedJobs.addAllBulkJob(userReq);
                                } catch (FailedItemsCollection.IllegalValueException ex1) {
                                    logger.log(ex.getSeverity(), "Unable to store failed row. " + ex1.getMessage());
                                }
                            } else {
                                System.err.print(ex.getMessage() + ex.getDetailedMessage());
                                System.exit(ex.getSeverity() - 1);
                            }
                        }
                    }
                } catch (UserBulkJobRequest.TaskLimitExceeded ex) {
                    logger.error("Too many user tasks in request, should not happen!");
                    System.exit(1);
                } catch (IntercomValidationException ex) {
                    logger.error(ex.getDetailedMessage());
                    System.exit(1);
                    //always quit on validation error 
                    /*
                    try {
                        failedJobs.addAllBulkJob(userReq);
                    } catch (FailedItemsCollection.IllegalValueException ex1) {
                        logger.warning("Unable to store failed row. ");
                    }*/

                }
            }//END send data
            try {
                csvreader.close();
            } catch (IOException ex) {
            }

            /*Send last bulk job*/
            if (!userReq.getItems().isEmpty()) {
                try {
                    String jobId = client.submitUserBulkJobRequest(userReq);
                    //collect job Id
                    jobIds.add(jobId);
                    userReq = new UserBulkJobRequest();
                } catch (ClientException ex) {
                    if (ex.getSeverity() < 2) {
                        logger.log(ex.getSeverity(), ex.getMessage());
                        try {
                            failedJobs.addAllBulkJob(userReq);
                        } catch (FailedItemsCollection.IllegalValueException ex1) {
                            logger.log(ex.getSeverity(), "Unable to store failed row. " + ex1.getMessage());
                        }
                    } else {
                        System.err.print(ex.getDetailedMessage());
                        System.exit(ex.getSeverity() - 1);
                    }
                }
            }
            logger.info((jobIds.size() - unproccessedJobsCount) + " bulk jobs successfuly submited.");

            /*Retrieve results*/
            logger.info("Retrieving results.");
            try {
                failedJobs.addAll(
                        client.waitAndCollectResults(jobIds, MAX_RUN_TIME - getRuntime(), REQ_WAIT_INTERVAL)
                );
            } catch (ClientException ex) {
                logger.error(ex.getDetailedMessage());
            } catch (FailedItemsCollection.IllegalValueException ex) {
                logger.error(ex.getMessage());
                System.exit(1);
            }

        } catch (KBCException ex) {
            if (ex.getSeverity() == 0) {
                logger.log(ex);
            } else {
                System.err.print(ex.getDetailedMessage());
                System.exit(ex.getSeverity());
            }
        } catch (IOException | SuperCsvException ex) {
            System.err.print("Unable to read the input table! " + ex.getMessage());
            System.exit(1);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            System.err.print(ex.getMessage());
        } finally {
            /*Set state file if some unfinished jobs*/
            if (!jobIds.isEmpty()) {
            	//TODO FIXME.
                IntercomWrLastState state = new IntercomWrLastState(jobIds); //new IntercomWrLastState(jobIds); 
                try {
                    handler.writeStateFile(state);
                } catch (KBCException ex) {
                    logger.log(ex);
                }
            }

            /*write error results table*/
            try {

                if (!failedJobs.isEmpty()) {
                    File failedJobsCsv = failedJobs.saveToCsv(handler.getOutputTablesPath());
                    handler.writeManifestFile(new ManifestFile.Builder(failedJobsCsv.getName(), null)
                            .setPrimaryKey(new String[]{"user_id", "job_run_timestamp"}).setIncrementalLoad(true).build());
                    logger.warning("Task completed with " + failedJobs.getItems().size() + " errors. Failed rows stored in user_errors.csv table.");
                }

                if (jobIds.isEmpty()) {
                    logger.info("All tasks completed successfuly");
                } else {
                    logger.warning((jobIds.size() + unproccessedJobsCount) + " jobs have not finished within the time limit. Results will be collected on the next run.");
                }

                if (csvreader != null) {

                    csvreader.close();
                }
            } catch (IOException ex) {
            	logger.error(ex.getMessage());
            } catch (KBCException ex) {
                logger.error(ex.getDetailedMessage());
            } catch (RuntimeException ex) {

            }
        }
    }

    private static long START_TIME;

    private static void startCounter() {
        START_TIME = Instant.now().getEpochSecond();
    }

    private static long getRuntime() {
        return Instant.now().getEpochSecond() - START_TIME;
    }
}
