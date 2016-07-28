/*
 */
package esnerda.keboola.intercom.writer.client;

/**
 *
 * author David Esner <esnerda at gmail.com>
 * created 2016
 */
public class IntercomValidationException extends Exception {

    private String detailedMessage;
    private String rowId;

    public IntercomValidationException(String message, String detailedMessage, String rowId) {
        super(message);
        this.detailedMessage = detailedMessage;
        this.rowId = rowId;

    }

    public String getDetailedMessage() {
        return detailedMessage;
    }

    public String getRowId() {
        return rowId;
    }

}
