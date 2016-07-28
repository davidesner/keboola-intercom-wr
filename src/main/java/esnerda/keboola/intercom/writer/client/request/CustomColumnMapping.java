/*
 */
package esnerda.keboola.intercom.writer.client.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.EnumUtils;

/**
 *
 * author David Esner <esnerda at gmail.com>
 * created 2016
 */
public class CustomColumnMapping {

    private final String srcCol;
    private final String destCol;
    @JsonIgnore
    private final Type dataType;
    private final boolean isNew;

    public CustomColumnMapping(String srcCol, String destCol, Type dataType, boolean isNew) {
        this.srcCol = srcCol;
        this.destCol = destCol;
        this.dataType = dataType;
        this.isNew = isNew;
    }

    @JsonCreator
    public CustomColumnMapping(@JsonProperty("srcCol") String srcCol, @JsonProperty("destCol") String destCol, @JsonProperty("dataType") String dataType,
            @JsonProperty("isNew") boolean isNew) {
        this.srcCol = srcCol;
        this.destCol = destCol;
        if (EnumUtils.isValidEnum(Type.class, dataType)) {
            this.dataType = Type.valueOf(dataType);
        } else {
            this.dataType = Type.String;
        }
        this.isNew = isNew;
    }

    public String getSrcCol() {
        return srcCol;
    }

    public String getDestCol() {
        return destCol;
    }

    public Type getDataType() {
        return dataType;
    }

    public boolean isIsNew() {
        return isNew;
    }

    public static enum Type {
        Boolean, Double, Float, Integer, Long, String
    }
}
