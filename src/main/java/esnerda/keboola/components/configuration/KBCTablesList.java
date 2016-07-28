/*
 */
package esnerda.keboola.components.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * author David Esner <code>&lt;esnerda at gmail.com&gt;</code>
 * created 2015
 */
public class KBCTablesList {

    @JsonProperty("tables")
    private List<KBCOutputMapping> tables;

    public KBCTablesList() {
    }

    public KBCTablesList(List<KBCOutputMapping> tables) {
        this.tables = tables;
    }

    public List<KBCOutputMapping> getTables() {
        return tables;
    }

    public void setTables(List<KBCOutputMapping> tables) {
        this.tables = tables;
    }
}
