/*
 */
package esnerda.keboola.components.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * author David Esner <code>&lt;esnerda at gmail.com&gt;</code>
 * created 2015
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class KBCConfig {

    String validationError;
    @JsonProperty("storage")
    private KBCStorage storage;
    @JsonProperty("parameters")
    private IKBCParameters params;

    public KBCConfig() {
        validationError = null;
    }

    public KBCConfig(KBCStorage storage, IKBCParameters params) {
        this.storage = storage;
        this.params = params;
    }

    public boolean validate() {
        try {
            return params.validateParametres();
        } catch (ValidationException ex) {
            this.validationError = ex.getDetailedMessage();
            return false;
        }

    }

    public String getValidationError() {
        return validationError;
    }

    private void setValidationError(List<String> missingFields) {
        this.validationError = "Required config fields are missing: ";
        int i = 0;
        for (String fld : missingFields) {
            if (i < missingFields.size()) {
                this.validationError += fld + ", ";
            } else {
                this.validationError += fld;
            }
        }
    }

    public KBCStorage getStorage() {
        return storage;
    }

    public void setStorage(KBCStorage storage) {
        this.storage = storage;
    }

    public IKBCParameters getParams() {
        return params;
    }

    public void setParams(IKBCParameters params) {
        this.params = params;
    }

    public List<KBCOutputMapping> getOutputTables() {
        return this.storage.getOutputTables().getTables();
    }
}
