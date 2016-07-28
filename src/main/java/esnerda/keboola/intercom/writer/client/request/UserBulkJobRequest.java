/*
 */
package esnerda.keboola.intercom.writer.client.request;

import io.intercom.api.JobItem;
import io.intercom.api.User;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * author David Esner <esnerda at gmail.com>
 * created 2016
 */
public class UserBulkJobRequest {

    private final List<JobItem<User>> items;
    private final static int TASK_LIMIT = 100;

    public UserBulkJobRequest() {
        this.items = new ArrayList<>();
    }

    public boolean isFull() {
        return (this.items.size() >= TASK_LIMIT);
    }

    public void addPostItem(User user) throws TaskLimitExceeded {
        if (this.items.size() >= TASK_LIMIT) {
            throw new TaskLimitExceeded("Cannot add more task into the bulk job. One bulk job can only proccess " + TASK_LIMIT + "items.");
        }

        this.items.add(new JobItem("post", user));
    }

    public List<JobItem<User>> getItems() {
        return items;
    }

    public class TaskLimitExceeded extends Exception {

        public TaskLimitExceeded(String message) {
            super(message);
        }

    }
}
