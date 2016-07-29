/*
 */
package esnerda.keboola.intercom.writer.client.request;

import io.intercom.api.CustomAttribute;
import io.intercom.api.JobItem;
import io.intercom.api.User;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * author David Esner <esnerda at gmail.com>
 * created 2016
 */
public class FailedUserBulkRequestItem extends FailedBulkRequestItem {

    private final Map<String, Object> parametersMap;
    private final Map<String, String> errorMessages;

    public FailedUserBulkRequestItem(JobItem<User> data) {
        super("User", data.getData().getUserId());
        this.parametersMap = new HashMap<>();
        User u = data.getData();

        this.parametersMap.put("user_id", u.getUserId());
        this.parametersMap.put("created_at", Long.toString(u.getCreatedAt()));
        this.parametersMap.put("email", u.getEmail());
        this.parametersMap.put("name", u.getName());
        this.parametersMap.put("type", u.getType());
        this.parametersMap.put("job_run_timestamp", Instant.ofEpochSecond(data.getUpdatedAt()));
        if (u.getCustomAttributes() != null) {
            for (Entry<String, CustomAttribute> e : u.getCustomAttributes().entrySet()) {
                if (e.getValue() != null) {
                    this.parametersMap.put(e.getKey(), e.getValue().getValue());
                }
            }
        }

        this.errorMessages = new HashMap<>();
        int i = 0;
        for (io.intercom.api.Error e : data.getError()) {
            this.errorMessages.put("error" + i, e.getMessage());
            i++;
        }

    }

    @Override
    public Map<String, Object> getParametersMap() {
        return this.parametersMap;
    }

    @Override
    public Map<String, String> getErrorMessages() {
        return this.errorMessages;
    }

}
