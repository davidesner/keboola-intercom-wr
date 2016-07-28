/*
 */
package esnerda.keboola.intercom.writer.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import esnerda.keboola.components.configuration.ValidationException;
import esnerda.keboola.intercom.writer.client.request.CompanyObjectBuilder;
import esnerda.keboola.intercom.writer.client.request.CustomColumnMapping;
import esnerda.keboola.intercom.writer.client.request.UserObjectBuilder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.EnumUtils;

/**
 *
 * author David Esner <esnerda at gmail.com>
 * created 2016
 */
public class CompanyMapping {

    private List<ColumnMapping> standardColumns;

    private List<CustomColumnMapping> customColumns;

    private Map<CompanyObjectBuilder.CompanyStaticColumns, String> standardColumnMapping;

    public CompanyMapping() {
        this.standardColumns = new ArrayList<>();
        this.customColumns = new ArrayList<>();
        this.standardColumns = new ArrayList<>();
    }

    public CompanyMapping(@JsonProperty("standardColumns") List<ColumnMapping> standardColumns, @JsonProperty("customColumns") List<CustomColumnMapping> customColumns) {
        this.standardColumns = standardColumns;
        this.customColumns = customColumns;

        //process Company columns mapping 
        if (standardColumns != null) {
            this.standardColumnMapping = new HashMap();
            for (ColumnMapping cm : this.standardColumns) {
                if (EnumUtils.isValidEnum(CompanyObjectBuilder.CompanyStaticColumns.class, cm.getDestCol().toLowerCase())) {
                    this.standardColumnMapping.put(CompanyObjectBuilder.CompanyStaticColumns.valueOf(cm.getDestCol().toLowerCase()), cm.getSrcCol());
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
            if (!EnumUtils.isValidEnum(CompanyObjectBuilder.CompanyStaticColumns.class, cm.getDestCol().toLowerCase())) {
                error += "\nColumn " + cm.getDestCol() + " is not valid destination column name. ";
            }
            if (!destCols.add(cm.getDestCol())) {
                error += "\nMapping for destination standard company column: " + cm.getDestCol() + " is specified more than one time!";
            }
            if (!srcCols.add(cm.getSrcCol())) {
                error += "\nMapping for src standard company column: " + cm.getSrcCol() + " is specified more than one time!";
            }
        }
        /*Check for custom columns mapping duplicities*/
        for (CustomColumnMapping ccm : this.customColumns) {

            if (!destCols.add(ccm.getDestCol())) {
                error += "\nMapping for destination custom company column: " + ccm.getDestCol() + " is specified more than one time!";
            }
            if (!srcCols.add(ccm.getSrcCol())) {
                error += "\nMapping for src custom company column: " + ccm.getSrcCol() + " is specified more than one time!";
            }
        }

        return error;
    }

    public String validateHeaderMapping(String[] header) throws ValidationException {
        String mappingError = "";
        Set<String> headerSet = new HashSet();
        //convert to lowercase
        Arrays.asList(header).forEach((String k) -> headerSet.add(k.toLowerCase()));
        if (this.standardColumnMapping.isEmpty()) {
            if (!headerSet.contains(CompanyObjectBuilder.CompanyStaticColumns.company_id.name())) {
                mappingError += "Standard mapping is empty, required column company_id does not exist in the input file!\n";
            }
        } else {
            if (!this.standardColumnMapping.containsKey(CompanyObjectBuilder.CompanyStaticColumns.company_id)) {
                mappingError += "Required column company_id does not exist in the input file!\n";
            }
            for (Map.Entry<CompanyObjectBuilder.CompanyStaticColumns, String> h : standardColumnMapping.entrySet()) {
                if (!headerSet.contains(h.getValue().toLowerCase())) {
                    mappingError += "Company column name " + h + " is invalid, it is specified in standard column mapping but not present in input. \n";
                }
            }
        }

        if (this.customColumns != null && !this.customColumns.isEmpty()) {
            for (CustomColumnMapping ccm : this.customColumns) {
                if (!headerSet.contains(ccm.getSrcCol().toLowerCase())) {
                    mappingError += "Source comapny column name " + ccm.getSrcCol() + " is invalid, it is specified in custom column mapping but not present in input. \n";
                }
            }
        }
        return mappingError;
    }

    public List<ColumnMapping> getStandardColumns() {
        return standardColumns;
    }

    public List<CustomColumnMapping> getCustomColumns() {
        return customColumns;
    }

    public Map<CompanyObjectBuilder.CompanyStaticColumns, String> getStandardColumnMapping() {
        return standardColumnMapping;
    }

}
