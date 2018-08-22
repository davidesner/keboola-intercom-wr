/*
 */
package esnerda.keboola.intercom.writer;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.supercsv.exception.SuperCsvException;
import org.supercsv.io.CsvMapReader;
import org.supercsv.io.ICsvMapReader;
import org.supercsv.prefs.CsvPreference;

import esnerda.keboola.components.KBCException;
import esnerda.keboola.components.configuration.handler.ConfigHandlerBuilder;
import esnerda.keboola.components.configuration.handler.KBCConfigurationEnvHandler;
import esnerda.keboola.components.logging.DefaultLogger;
import esnerda.keboola.components.logging.KBCLogger;
import esnerda.keboola.intercom.writer.client.Client;
import esnerda.keboola.intercom.writer.client.IntercomValidationException;
import esnerda.keboola.intercom.writer.client.request.UserObjectBuilder;
import esnerda.keboola.intercom.writer.config.IntercomWrLastState;
import esnerda.keboola.intercom.writer.config.IntercomWrParameters;
import esnerda.keboola.intercom.writer.config.UserMapping;
import io.intercom.api.User;

/**
 *
 * @author David Esner <esnerda at gmail.com>
 * @created 2016
 */
public class Writer {

	// max run time before give up result collection (Seconds)
	private final static long MAX_RUN_TIME = 19800;
	private static SimpleTimer timer = new SimpleTimer(MAX_RUN_TIME * 1000);

	static KBCLogger logger = new DefaultLogger(Writer.class);

	public static void main(String[] args) {

		timer.startTimer();

		if (args.length == 0) {
			logger.error("No parameters provided.", null);
			System.exit(2);
		}

		IntercomWrParameters confParams = null;
		File usersInput = null;
		KBCConfigurationEnvHandler handler;
		try {
			handler = initEnvironment(args[0]);
			confParams = (IntercomWrParameters) handler.getParameters();
			if (handler.getInputTables().isEmpty()) {
				throw new KBCException("Input mapping is missing!", 2, null);
			}
			usersInput = handler.getInputTables().get(0).getCsvTable();
		} catch (KBCException ex) {
			handleKbcException(ex);
		}

		Client client = new Client(confParams.getAppId(), confParams.getApiKey());

		try (ICsvMapReader csvreader = new CsvMapReader(new FileReader(usersInput),
				CsvPreference.STANDARD_PREFERENCE)) {
			/* Proccess input table */
			// get header
			final String[] header = csvreader.getHeader(true);

			/* validate mapping */
			UserMapping userMapping = confParams.getUserColMapping();
			userMapping.validateHeaderMapping(header);

			/* Send data */
			logger.info("Submiting User data...");
			Map<String, String> userData = new HashMap<String, String>();

			int recordCnt = 0;
			while ((userData = csvreader.read(header)) != null) {
				// check timeout
				if (timer.isTimedOut()) {
					logger.error("Excecution timed out! Only " + recordCnt
							+ " records were updated. Use less records in single job next time!", null);
					System.exit(1);
				}

				// insert user item to a request
				User user = new UserObjectBuilder(userData, userMapping.getStandardColumnMapping())
						.setUserCustomColumns(userData, userMapping.getCustomColumns())
						.setCompany(userData, userMapping.getCompanyColumns().getStandardColumnMapping(),
								userMapping.getCompanyColumns().getCustomColumns())
						.build();
				client.upsertUser(user);
				// counter
				recordCnt++;
			}//end loop

			logger.info(recordCnt + " records updated successfully!");

		} catch (IntercomValidationException ex) {
			logger.error(ex.getDetailedMessage(), ex);
			System.exit(1);
		} catch (KBCException ex) {
			handleKbcException(ex);
		} catch (IOException | SuperCsvException ex) {
			logger.error("Unable to read the input table! " + ex.getMessage(), ex);
			System.exit(2);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			System.exit(2);
		}

	}

	private static void handleKbcException(KBCException ex) {
		logger.log(ex);
		if (ex.getSeverity() > 1) {
			System.exit(ex.getSeverity() - 1);
		}
	}

	private static KBCConfigurationEnvHandler initEnvironment(String dataPath) throws KBCException {
		KBCConfigurationEnvHandler handler = ConfigHandlerBuilder.create(IntercomWrParameters.class)
				.hasInputTables(true).setStateFileType(IntercomWrLastState.class).build();
		// process the configuration
		handler.processConfigFile(new File(dataPath));
		return handler;

	}
}
