/*
 */
package esnerda.keboola.intercom.writer.client;

import esnerda.keboola.components.KBCException;

/**
 *
 * author David Esner <esnerda at gmail.com>
 * created 2016
 */
public class ClientException extends KBCException{



    public ClientException(int severity, String detailedMessage, Object details, String message) {
    	super(message, detailedMessage, details, severity);

    }

}
