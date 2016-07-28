/*
 */
package esnerda.keboola.intercom.writer.client;

/**
 *
 * author David Esner <esnerda at gmail.com>
 * created 2016
 */
public class ClientException extends Exception {

    private final int severity;

    private final String detailedMessage;

    private final Object details;

    public ClientException(int severity, String detailedMessage, Object details, String message) {
        super(message);
        this.severity = severity;
        this.detailedMessage = detailedMessage;
        this.details = details;
    }

    public int getSeverity() {
        return severity;
    }

    public String getDetailedMessage() {
        return detailedMessage;
    }

    public Object getDetails() {
        return details;
    }

}
