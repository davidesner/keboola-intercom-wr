/*
 */
package esnerda.keboola.intercom.writer.config;

/**
 *
 * author David Esner <esnerda at gmail.com>
 * created 2016
 */
public class ColumnMapping {

    private String srcCol;
    private String destCol;

    public ColumnMapping() {
    }

    public String getSrcCol() {
        return srcCol;
    }

    public void setSrcCol(String srcCol) {
        this.srcCol = srcCol;
    }

    public String getDestCol() {
        return destCol;
    }

    public void setDestCol(String destCol) {
        this.destCol = destCol;
    }
}
