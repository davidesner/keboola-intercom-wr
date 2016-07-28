/*
 */
package esnerda.keboola.components.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * author David Esner <code>&lt;esnerda at gmail.com&gt;</code>
 * created 2016
 */
public abstract class IKBCParameters {

    /**
     * Returns list of required fields missing in config
     *
     * @return
     */
    private List<String> getMissingFields(Map<String, Object> parametersMap) {

        List<String> missing = new ArrayList<>();
        for (String requiredField : getRequiredFields()) {
            Object value = parametersMap.get(requiredField);
            if (value == null) {
                missing.add(requiredField);
            }
        }

        if (missing.isEmpty()) {
            return null;
        }
        return missing;
    }

    protected final String missingFieldsMessage(Map<String, Object> parametersMap) {

        List<String> missingFields = getMissingFields(parametersMap);
        String msg = "";
        if (missingFields != null && missingFields.size() > 0) {
            msg = "Required config fields are missing: ";
            int i = 0;
            for (String fld : missingFields) {
                if (i < missingFields.size()) {
                    msg += fld + ", ";
                } else {
                    msg += fld;
                }
            }
        }
        return msg;
    }

    protected abstract String[] getRequiredFields();

    protected abstract boolean validateParametres() throws ValidationException;
}
