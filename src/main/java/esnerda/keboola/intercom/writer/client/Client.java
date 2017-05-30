/*
 */
package esnerda.keboola.intercom.writer.client;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LoggerContext;

import esnerda.keboola.intercom.writer.SimpleTimer;
import esnerda.keboola.intercom.writer.client.request.CustomColumnMapping;
import esnerda.keboola.intercom.writer.client.request.FailedBulkRequestItem;
import esnerda.keboola.intercom.writer.client.request.FailedUserBulkRequestItem;
import esnerda.keboola.intercom.writer.client.request.UserBulkJobRequest;
import io.intercom.api.AuthorizationException;
import io.intercom.api.CustomAttribute;
import io.intercom.api.Intercom;
import io.intercom.api.IntercomException;
import io.intercom.api.InvalidException;
import io.intercom.api.Job;
import io.intercom.api.JobItem;
import io.intercom.api.JobItemCollection;
import io.intercom.api.JobTask;
import io.intercom.api.RateLimitException;
import io.intercom.api.ServerException;
import io.intercom.api.User;
import io.intercom.api.UserCollection;

/**
 *
 * author David Esner <esnerda at gmail.com>
 * created 2016
 */
public class Client {

    private final static long BACKOFF_INTERVAL = 500;
    private final static int RETRIES = 5;
    private static long WAIT_INTERVAL = 0;

    public Client(String appId, String apiKey) {
        Intercom.setAppID(appId);
        Intercom.setToken(apiKey);

        LoggerContext.getContext().getLogger("intercom-java").setLevel(Level.OFF);
    }

    /**
     * Submits bulk user request. Return resulting job ID if successfull
     *
     * @param req
     * @return
     * @throws ClientException
     */
    public String submitUserBulkJobRequest(UserBulkJobRequest req) throws ClientException {
        String lastException = "";
        Job job = null;
        boolean success = false;
        int retries = 0;
        /*Submit items, try to recover on some*/
        waitNmilis(WAIT_INTERVAL);//optional wait interval

        while (!success && retries <= RETRIES) {
            if (Intercom.getRateLimitDetails().canSubmit()) {
                try {

                    job = User.submit(req.getItems());
                    success = true;

                } catch (RateLimitException rex) {
                    waitNmilis(Intercom.getRateLimitDetails().getRemainingMilis() + 1);
                    retries++;
                } catch (AuthorizationException ex) {
                    throw new ClientException(2, ex.getMessage(), ex.getErrorCollection(), "Authorization error, check your credentials! Do you use Personal Token instead of ApiKey?");
                } catch (ServerException ex) {
                    waitNmilis(BACKOFF_INTERVAL);
                    retries++;
                } catch (io.intercom.api.ClientException | InvalidException ex) {
                    throw new ClientException(1, ex.getMessage(), ex.getErrorCollection(), "Unable to submit job!");
                } catch (IntercomException ex) {
                    if (retries >= RETRIES - 1) {
                        throw new ClientException(1, ex.getMessage(), ex.getErrorCollection(), "Unable to submit job after several tries!");
                    }

                    waitNmilis(BACKOFF_INTERVAL);
                    retries++;
                }

            } else {
                lastException = Intercom.getRateLimitDetails().toString();
                //wait until rate limit renewed
                waitNmilis(Intercom.getRateLimitDetails().getRemainingMilis() + 1);
                retries++;
            }

        }
        if (job == null) {
            throw new ClientException(1, "Unable to sumbit job, requests failed after " + RETRIES + " retries. " + lastException, null, "Unable to submit job!");
        }
        return job.getID();

    }

    public List<FailedBulkRequestItem> getFailedUserJobItems(String jobId) throws ClientException {
        String lastException = "";
        List<FailedBulkRequestItem> failedItems = new ArrayList<>();

        Job job = getJobById(jobId);
        List<JobItem<User>> failedUItems = new ArrayList();
        boolean success = false;
        int retries = 0;
        JobItemCollection jc = null;
        while (!success && retries <= RETRIES) {
            try {
                if (Intercom.getRateLimitDetails().canSubmit()) {
                    jc = User.listJobErrorFeed(job.getID());
                    success = true;
                } else {
                    lastException = Intercom.getRateLimitDetails().toString();
                    //wait until rate limit renewed
                    waitNmilis(Intercom.getRateLimitDetails().getRemainingMilis() + 1);
                    retries++;
                }
            } catch (RateLimitException rex) {
                lastException = rex.getMessage();
                waitNmilis(Intercom.getRateLimitDetails().getRemainingMilis() + 1);
                retries++;
            } catch (AuthorizationException ex) {
                throw new ClientException(2, ex.getMessage(), ex.getErrorCollection(), "AAuthorization error, check your credentials! Do you use Personal Token instead of ApiKey?");
            } catch (ServerException ex) {
                lastException = ex.getMessage();
                waitNmilis(BACKOFF_INTERVAL);
                retries++;
            } catch (io.intercom.api.ClientException | InvalidException ex) {
                throw new ClientException(1, ex.getMessage(), ex.getErrorCollection(), "Unable to submit job!");
            } catch (IntercomException ex) {
                lastException = ex.getMessage();
                if (retries >= RETRIES - 1) {
                    throw new ClientException(1, ex.getMessage(), ex.getErrorCollection(), "Unable to submit job after several tries!");
                }

                waitNmilis(BACKOFF_INTERVAL);
                retries++;
            }
        }

        if (jc == null) {
            throw new ClientException(1, "Unable to retrieve error feed, requests failed after " + RETRIES + " retries. " + lastException, null, "Unable to submit job!");
        }
        failedUItems.addAll(jc.getPage());
        while (jc.hasNextPage()) {
            failedUItems.addAll(jc.nextPage().getPage());
        }
		if (!failedUItems.isEmpty()) {
			for (JobItem<User> jb : failedUItems) {
				failedItems.add(new FailedUserBulkRequestItem(jb));
			}
		}

        return failedItems;
    }

    public List<FailedBulkRequestItem> waitAndCollectResults(List<String> jobIds, long runTimeSeconds, long waitIntervalMilis) throws ClientException {
        List<FailedBulkRequestItem> failedJobs = new ArrayList<>();       
        SimpleTimer tmr = new SimpleTimer(runTimeSeconds * 1000);
        tmr.startTimer();
        boolean flag = false;
        while (!flag && !tmr.isTimedOut()) {
        	flag = true;
        	List<String> unfinishedJobs = new ArrayList<>(jobIds);
           for(String jobId : unfinishedJobs) {      
                //wait between each check
                SimpleTimer.reallySleep(waitIntervalMilis);
                if (isJobCompleted(jobId)) {
                	jobIds.remove(jobId);
                    failedJobs.addAll(getFailedUserJobItems(jobId));                   
                } else {
                	flag = false;
                }
            }

        }
        return failedJobs;
    }

    private Job getJobById(String id) throws ClientException {
        boolean success = false;
        int retries = 0;
        String lastException = "";
        Job job = null;

        while (!success && retries <= RETRIES) {
            if (Intercom.getRateLimitDetails().canSubmit()) {
                try {
                    job = Job.find(id);
                    success = true;
                } catch (RateLimitException rex) {
                    lastException = rex.getMessage();
                    waitNmilis(Intercom.getRateLimitDetails().getRemainingMilis() + 1);
                    retries++;
                } catch (AuthorizationException ex) {
                    throw new ClientException(2, ex.getMessage(), ex.getErrorCollection(), "Authorization error, check your credentials! Do you use Personal Token instead of ApiKey?");
                } catch (ServerException ex) {
                    lastException = ex.getMessage();
                    waitNmilis(BACKOFF_INTERVAL);
                    retries++;
                } catch (io.intercom.api.ClientException | InvalidException ex) {
                    throw new ClientException(1, ex.getMessage(), ex.getErrorCollection(), "Unable to retrieve job info!");
                } catch (IntercomException ex) {
                    lastException = ex.getMessage();
                    if (retries >= RETRIES - 1) {
                        throw new ClientException(1, ex.getMessage(), ex.getErrorCollection(), "Unable to retrieve job info after several tries!");
                    }
                    waitNmilis(BACKOFF_INTERVAL);
                    retries++;
                }
            } else {
                lastException = Intercom.getRateLimitDetails().toString();
                //wait until rate limit renewed
                waitNmilis(Intercom.getRateLimitDetails().getRemainingMilis() + 1000);
                retries++;
            }
        }

        if (job == null) {
            throw new ClientException(1, "Unable to send request (Job state),failed after " + RETRIES + " retries. " + lastException, null, "Unable to send request!");
        }
        return job;
    }

    /**
     * Checks for a job status and returns true if the job is completed
     *
     * @param jobId
     * @return
     * @throws ClientException
     */
    public boolean isJobCompleted(String jobId) throws ClientException {
        Job job = getJobById(jobId);
        if (job.getState().equals("completed") || job.getState().equals("completed_with_errors")) {
            //job is finished
            return true;
        }
		//job still open but all tasks had finished
		List<JobTask> tasks = job.getTasks();
		if (tasks != null) {

		    return tasks.stream().allMatch((JobTask task) -> {
		        return (task.getCompletedAt() != null) && (Instant.now().getEpochSecond() >= task.getCompletedAt());
		    }
		    );
		}
		return false;
    }

	private void waitNmilis(long interval) {
		if (interval < 0) {
			return;
		}
		// wait until rate limit renewed
		SimpleTimer.reallySleep(interval);

	}

    /**
     * Sets the interval between each request. Saves Api's resources.
     *
     * @param interval - wait time in miliseconds
     */
    public void setWaitInterval(long interval) {
        WAIT_INTERVAL = interval;
    }

    public void validateUserCustomAttributes(List<CustomColumnMapping> userColNames, List<CustomColumnMapping> companyCustColNames) throws InvalidAttributeNamesException {
        Map<String, String> params = new HashMap<>();
        params.put("per_page", "1");

        UserCollection users = User.list(params);
        User testU = users.next();

        Map<String, CustomAttribute> attrMap = testU.getCustomAttributes();
        List<String> invColumns = new ArrayList();
        List<String> invCompanyColumns = new ArrayList();
        for (CustomColumnMapping srcCol : userColNames) {

            if (!attrMap.containsKey(srcCol.getDestCol()) && !srcCol.isIsNew()) {
                invColumns.add(srcCol.getSrcCol());
            }
        }

        if (testU.getCompanyCollection() != null && testU.getCompanyCollection().hasNext()) {
            attrMap = testU.getCompanyCollection().next().getCustomAttributes();
        }

        for (CustomColumnMapping srcCol : companyCustColNames) {

            if (!attrMap.containsKey(srcCol.getDestCol()) && !srcCol.isIsNew()) {
                invColumns.add(srcCol.getSrcCol());
            }
        }

        if (!invColumns.isEmpty()) {
            throw new InvalidAttributeNamesException("Invalid custom attributes names!", "Some User custom attributes names are invalid! Please check your configuration and use existing names or set mapping parameter to 'create' accordingly.", invColumns);
        }

    }
}
