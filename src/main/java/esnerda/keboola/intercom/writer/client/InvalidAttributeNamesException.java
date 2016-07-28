/*
 */
package esnerda.keboola.intercom.writer.client;

import java.util.List;

/**
 *
 * author David Esner <esnerda at gmail.com>
 * created 2016
 */
public class InvalidAttributeNamesException extends Exception {

    private final String detailedMessage;
    private List<String> invalidAttributes;

    public InvalidAttributeNamesException(String message, String detailedMessage, List<String> invalidAttributes) {
        super(message);
        this.detailedMessage = detailedMessage;

    }
}
