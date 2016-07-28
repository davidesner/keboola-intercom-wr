/*
 */
package esnerda.keboola.components.configuration;

import esnerda.keboola.components.KBCException;

/**
 *
 * author David Esner <code>&lt;esnerda at gmail.com&gt;</code>
 * created 2016
 */
public class ValidationException extends KBCException {

    public ValidationException(String message, String detailedMessage, Object details) {
        super(message, detailedMessage, details);
    }

}
