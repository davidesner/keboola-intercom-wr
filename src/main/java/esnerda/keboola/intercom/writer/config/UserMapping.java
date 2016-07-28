/*
 */
package esnerda.keboola.intercom.writer.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import esnerda.keboola.components.configuration.ValidationException;
import esnerda.keboola.intercom.writer.client.request.CustomColumnMapping;
import esnerda.keboola.intercom.writer.client.request.UserObjectBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.commons.lang3.EnumUtils;

/**
 *
 * author David Esner <esnerda at gmail.com>
 * created 2016
 */
public class UserMapping {

    private List<ColumnMapping> standardColumns;

    private List<CustomColumnMapping> customColumns;

    private CompanyMapping companyColumns;

    @JsonIgnore
    Map<UserObjectBuilder.UserStaticColumns, String> standardColumnMapping;

    public UserMapping() {
    }

    public UserMapping(@JsonProperty("standardColumns") List<ColumnMapping> standardColumns, @JsonProperty("customColumns") List<CustomColumnMapping> customColumns,
            @JsonProperty("companyColumnMapping") CompanyMapping companyColumns) {
        this.standardColumnMapping = new HashMap();
        this.standardColumns = standardColumns;
        if (customColumns != null) {
            this.customColumns = customColumns;
        } else {
            this.customColumns = new ArrayList<>();
        }

        this.companyColumns = companyColumns;

        //process user columns mapping 
        if (standardColumns != null) {

            for (ColumnMapping cm : this.standardColumns) {
                if (EnumUtils.isValidEnum(UserObjectBuilder.UserStaticColumns.class, cm.getDestCol().toLowerCase())) {
                    this.standardColumnMapping.put(UserObjectBuilder.UserStaticColumns.valueOf(cm.getDestCol().toLowerCase()), cm.getSrcCol());
                }
            }
        } else {
            this.standardColumnMapping = new HashMap();
        }
    }

    public String validateColumnMapping() {
        String error = "";
        //validate dest fields
        Set<String> destCols = new HashSet();
        Set<String> srcCols = new HashSet();
        /*Check for standard mapping duplicities*/
        for (ColumnMapping cm : this.standardColumns) {
            if (!EnumUtils.isValidEnum(UserObjectBuilder.UserStaticColumns.class, cm.getDestCol().toLowerCase())) {
                error += "\nColumn " + cm.getDestCol() + " is not valid destination column name. ";
            }
            if (!destCols.add(cm.getDestCol())) {
                error += "\nMapping for destination standard user column: " + cm.getDestCol() + " is specified more than one time!";
            }
            if (!srcCols.add(cm.getSrcCol())) {
                error += "\nMapping for src standard user column: " + cm.getSrcCol() + " is specified more than one time!";
            }
        }
        /*Check for custom columns mapping duplicities*/
        for (CustomColumnMapping ccm : this.customColumns) {

            if (!destCols.add(ccm.getDestCol())) {
                error += "\nMapping for destination custom user column: " + ccm.getDestCol() + " is specified more than one time!";
            }
            if (!srcCols.add(ccm.getSrcCol())) {
                error += "\nMapping for src custom user column: " + ccm.getSrcCol() + " is specified more than one time!";
            }
        }
        if (this.companyColumns != null) {
            error += this.companyColumns.validateColumnMapping();
        }
        return error;
    }

    /**
     * Validates given column names array towards current mapping configuration.
     *
     * @param header - array of column names
     * @throws ValidationException
     */
    public boolean validateHeaderMapping(String[] header) throws ValidationException {
        String mappingError = "";

        Set<String> headerSet = new HashSet();
        //convert to lowercase
        Arrays.asList(header).forEach((String k) -> headerSet.add(k.toLowerCase()));

        if (this.standardColumnMapping.isEmpty()) {
            if (!headerSet.contains(UserObjectBuilder.UserStaticColumns.user_id.name())) {
                mappingError += "Standard mapping is empty, required column user_id does not exist!\n";
            }
        } else {
            if (!standardColumnMapping.keySet().contains(UserObjectBuilder.UserStaticColumns.user_id)) {
                mappingError += "Required column user_id does not exist!\n";
            }
            for (Entry<UserObjectBuilder.UserStaticColumns, String> h : standardColumnMapping.entrySet()) {
                if (!headerSet.contains(h.getValue().toLowerCase())) {
                    mappingError += "Column name " + h + " is invalid, it is specified in standard column mapping but not present in the input file. \n";
                }
            }
        }

        if (this.customColumns != null && !this.customColumns.isEmpty()) {
            for (CustomColumnMapping ccm : this.customColumns) {
                if (!headerSet.contains(ccm.getSrcCol().toLowerCase())) {
                    mappingError += "Source column name " + ccm.getSrcCol() + " is invalid, it is specified in custom column mapping but not present in input. \n";
                }
            }
        }
        if (this.companyColumns != null) {
            mappingError += this.companyColumns.validateHeaderMapping(header);
        } else {
            this.companyColumns = new CompanyMapping();
        }
        if (mappingError.equals("")) {
            return true;
        } else {
            throw new ValidationException("Invalid mapping specified for given input specified!", mappingError, null);
        }

    }

    public List<ColumnMapping> getStandardColumns() {
        return standardColumns;
    }

    public List<CustomColumnMapping> getCustomColumns() {
        return customColumns;
    }

    public CompanyMapping getCompanyColumns() {
        return companyColumns;
    }

    public Map<UserObjectBuilder.UserStaticColumns, String> getStandardColumnMapping() {
        return standardColumnMapping;
    }

}
