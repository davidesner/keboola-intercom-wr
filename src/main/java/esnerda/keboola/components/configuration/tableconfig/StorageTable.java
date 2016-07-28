/*
 */
package esnerda.keboola.components.configuration.tableconfig;

import java.io.File;

/**
 * Wrapper class for KBC Storage Table mapping.
 *
 * author David Esner <code>&lt;esnerda at gmail.com&gt;</code>
 * created 2016
 */
public class StorageTable {

    private final ManifestFile metadata;
    private final File csvTable;

    public StorageTable(ManifestFile metadata, File csvTable) {
        this.metadata = metadata;
        this.csvTable = csvTable;
    }

    public ManifestFile getMetadata() {
        return metadata;
    }

    public File getCsvTable() {
        return csvTable;
    }

}
