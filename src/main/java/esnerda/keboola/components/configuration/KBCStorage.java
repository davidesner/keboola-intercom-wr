/*
 */
package esnerda.keboola.components.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * author David Esner <code>&lt;esnerda at gmail.com&gt;</code>
 * created 2015
 */
public class KBCStorage {

    @JsonProperty("input")
    private KBCTablesList inputTables;
    @JsonProperty("output")
    private KBCTablesList outputTables;

    public KBCStorage() {
    }

    public KBCStorage(KBCTablesList outputTables) {
        this.outputTables = outputTables;
    }

    public KBCTablesList getOutputTables() {
        return outputTables;
    }

    public void setOutputTables(KBCTablesList outputTables) {
        this.outputTables = outputTables;
    }

    public KBCTablesList getInputTables() {
        return inputTables;
    }

    public void setInputTables(KBCTablesList inputTables) {
        this.inputTables = inputTables;
    }

}
