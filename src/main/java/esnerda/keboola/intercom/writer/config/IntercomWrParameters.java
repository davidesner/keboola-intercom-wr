/*
 */
package esnerda.keboola.intercom.writer.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import esnerda.keboola.components.configuration.IKBCParameters;
import esnerda.keboola.components.configuration.ValidationException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.EnumUtils;

/**
 *
 * @author David Esner <esnerda at gmail.com>
 * @created 2015
 */
public class IntercomWrParameters extends IKBCParameters {

    private final static String[] REQUIRED_FIELDS = {"appId", "apiKey", "userColumnMapping"};
    private final Map<String, Object> parametersMap;

    @JsonProperty("appId")
    private String appId;
    @JsonProperty("#apiKey")
    private String apiKey;

    @JsonProperty("userColumnMapping")
    private UserMapping userColMapping;

    public IntercomWrParameters() {
        parametersMap = new HashMap();

    }

    @JsonCreator
    public IntercomWrParameters(@JsonProperty("appId") String appId, @JsonProperty("#apiKey") String apiKey,
            @JsonProperty("userColumnMapping") UserMapping userColMapping) throws ParseException {
        parametersMap = new HashMap();
        this.appId = appId;
        this.apiKey = apiKey;
        this.userColMapping = userColMapping;

        //set param map
        parametersMap.put("appId", appId);
        parametersMap.put("apiKey", apiKey);
        parametersMap.put("userColumnMapping", userColMapping);

    }

    @Override
    public boolean validateParametres() throws ValidationException {
        //validate date format
        String error = "";

        error += this.missingFieldsMessage(parametersMap);
        error += this.userColMapping.validateColumnMapping();
        class ValidationError {

            List<String> invalidUserStandardColumnNames;
            List<String> invalidCompanyStandardColumnNames;

            public ValidationError(List<String> invalidUserStandardColumnNames, List<String> invalidCompanyStandardColumnNames) {
                this.invalidUserStandardColumnNames = invalidUserStandardColumnNames;
                this.invalidCompanyStandardColumnNames = invalidCompanyStandardColumnNames;
            }

            public List<String> getInvalidUserStandardColumnNames() {
                return invalidUserStandardColumnNames;
            }

            public List<String> getInvalidCompanyStandardColumnNames() {
                return invalidCompanyStandardColumnNames;
            }

        }

        if (error.equals("")) {
            return true;
        } else {

            throw new ValidationException("Invalid configuration parameters!", "Config validation error: " + error, null);
        }
    }

    public String getAppId() {
        return appId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public UserMapping getUserColMapping() {
        return userColMapping;
    }

    public Map<String, Object> getParametersMap() {
        return parametersMap;
    }

    @Override
    protected String[] getRequiredFields() {
        return REQUIRED_FIELDS;
    }

}
