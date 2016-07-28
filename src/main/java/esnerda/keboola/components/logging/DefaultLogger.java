/*
 */
package esnerda.keboola.components.logging;

import esnerda.keboola.components.KBCException;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationFactory;

/**
 *
 * author David Esner <esnerda at gmail.com>
 * created 2016
 */
public class DefaultLogger implements KBCLogger {

    private final Logger logger;
    private LoggerContext CTX;

    public DefaultLogger(Class c) {

        ConfigurationFactory.setConfigurationFactory(new CustomConfigurationFactory());

        logger = LogManager.getLogger(c.getName());

    }

    @Override
    public void log(int severity, String shortMessage, String detailedMessage, Map<String, String> additionalFields) {
        switch (severity) {
            case 0:
                logger.info(detailedMessage);
                break;
            case 1:
                logger.warn(detailedMessage);
                break;
            case 2:
                logger.error(detailedMessage);
                break;
        }
        if (severity > 2) {
            logger.error(detailedMessage);
        }
    }

    @Override
    public void log(KBCException ex) {
        log(ex.getSeverity(), ex.getMessage(), ex.getDetailedMessage(), null);
    }

    @Override
    public void info(String shortMessage, String detailedMessage, Map<String, String> additionalFields) {
        logger.info(detailedMessage);
    }

    @Override
    public void warning(String shortMessage, String detailedMessage, Map<String, String> additionalFields) {
        logger.warn(detailedMessage);
    }

    @Override
    public void debug(String shortMessage, String detailedMessage, Map<String, String> additionalFields) {
        logger.debug(detailedMessage);
    }

    @Override
    public void error(String shortMessage, String detailedMessage, Map<String, String> additionalFields) {
        logger.error(detailedMessage);
    }

    @Override
    public void setLevel(Level lvl) {
        switch (lvl) {
            case debug:
                CTX.getConfiguration().getRootLogger().setLevel(org.apache.logging.log4j.Level.DEBUG);
                break;

            case normal:
                CTX.getConfiguration().getRootLogger().setLevel(org.apache.logging.log4j.Level.ERROR);
        }
    }

    @Override
    public void log(int severity, String message) {
        log(severity, "", message, null);
    }

    @Override
    public void info(String detailedMessage) {
        log(0, "", detailedMessage, null);
    }

    @Override
    public void warning(String detailedMessage) {
        log(1, "", detailedMessage, null);
    }

    @Override
    public void error(String detailedMessage) {
        log(2, "", detailedMessage, null);
    }

}
