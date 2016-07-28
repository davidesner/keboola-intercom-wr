/*
 */
package esnerda.keboola.intercom.writer.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import esnerda.keboola.components.appstate.LastState;
import java.util.List;

/**
 *
 * @author David Esner <esnerda at gmail.com>
 * @created 2016
 */
public class IntercomWrLastState implements LastState {

    @JsonProperty("unfinishedUserJobIds")
    private List<String> unfinishedUserJobIds;

    public IntercomWrLastState(List<String> unfinishedJobIds) {
        this.unfinishedUserJobIds = unfinishedJobIds;
    }

    public List<String> getUnfinishedUserJobIds() {
        return unfinishedUserJobIds;
    }

    public IntercomWrLastState() {
    }

}
