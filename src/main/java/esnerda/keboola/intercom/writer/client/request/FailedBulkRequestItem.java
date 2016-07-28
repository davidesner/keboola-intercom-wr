/*
 */
package esnerda.keboola.intercom.writer.client.request;

import io.intercom.api.JobItem;
import java.util.Map;

/**
 *
 * author David Esner <esnerda at gmail.com>
 * created 2016
 */
public abstract class FailedBulkRequestItem {

    private final String type;
    private final String id;

    public FailedBulkRequestItem(String type, String id) {
        this.type = type;
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public abstract Map<String, Object> getParametersMap();

    public abstract Map<String, String> getErrorMessages();
}
