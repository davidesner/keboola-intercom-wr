/*
 */
package esnerda.keboola.components.configuration.handler;

import esnerda.keboola.components.KBCException;
import esnerda.keboola.components.appstate.LastState;
import esnerda.keboola.components.appstate.LastStateWriter;
import esnerda.keboola.components.configuration.ConfigFormat;
import esnerda.keboola.components.configuration.parser.ConfigParser;
import esnerda.keboola.components.configuration.IKBCParameters;
import esnerda.keboola.components.configuration.KBCConfig;
import esnerda.keboola.components.configuration.KBCOutputMapping;
import esnerda.keboola.components.configuration.tableconfig.ManifestFile;
import esnerda.keboola.components.configuration.tableconfig.ManifestParser;
import esnerda.keboola.components.configuration.tableconfig.ManifestWriter;
import esnerda.keboola.components.configuration.tableconfig.StorageTable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Basic implementation of KBCConfigurationEnvHandler facet. Initializes and
 * manages
 * the standard KBC docker environment
 *
 * author David Esner <code>&lt;esnerda at gmail.com&gt;</code>
 * created 2016
 */
class KBCConfigurationHandlerImpl implements KBCConfigurationEnvHandler {

    private KBCConfig config;
    private final boolean checkInputTables;
    private final Class statefileType;
    private final Class parametersType;
    private final ConfigFormat format;
    private String dataPath;
    private String outputTablesPath;
    private String inTablesPath;
    private final ConfigParser confParser;

    private List<StorageTable> sourceTables;
    private LastState lastState;

    KBCConfigurationHandlerImpl(boolean checkInputTables, Class statefileType, Class parametersType, ConfigParser confParser, ConfigFormat format) {
        this.checkInputTables = checkInputTables;
        this.statefileType = statefileType;
        this.parametersType = parametersType;
        this.confParser = confParser;
        this.format = format;
    }

    /**
     * Process and validate the configuration file in specified directory.
     * Performs checks on existence
     * of input mappings, state files, etc. based on the Handler configuration
     * setting.
     *
     * The directory provided must maintain the Keboola Connection common
     * interface data folder structure. Configuration is expected to reside in
     * 'config.json' or 'config.yml' respectively, according to the format
     * setting.
     *
     * @param confFile - Directory with correct data structure.
     * @throws KBCException
     */
    @Override
    public void processConfigFile(File confFile) throws KBCException {

        if (!confFile.isDirectory()) {
            throw new KBCException("Specified path is not a folder!", "Specified data folder path: '" + confFile.getAbsoluteFile() + "' is not a folder!", confFile, 1);
        } else {
            this.dataPath = confFile.getPath();
            confFile = new File(confFile.getPath() + File.separator + "config." + confParser.getFormat().toLowerCase());

        }

        this.outputTablesPath = dataPath + File.separator + "out" + File.separator + "tables";
        this.inTablesPath = dataPath + File.separator + "in" + File.separator + "tables"; //parse config

        if (!confFile.exists()) {
            throw new KBCException(confFile.getName() + " does not exist!", "Configuration file " + confFile.getName() + "does not exist!", 1);
        }
        //Parse config file
        try {
            this.config = this.confParser.parseConfigFile(confFile, this.parametersType);

        } catch (Exception ex) {
            throw new KBCException("Failed to parse config file.", ex.getMessage(), ex);
        }
        if (!config.validate()) {
            throw new KBCException("Config file invalid!", config.getValidationError(), null);
        }

        if (checkInputTables) {
            getInputTables();
        }

        if (statefileType != null) {
            retrieveStateFile();
        }
    }

    private void retrieveStateFile() throws KBCException {
        File stateFile = new File(dataPath + File.separator + "in" + File.separator + "state." + confParser.getFormat().toLowerCase());
        if (stateFile.exists()) {
            try {
                lastState = (LastState) this.confParser.parseFile(stateFile, this.statefileType);
            } catch (IOException ex) {
                throw new KBCException("Unable to parse state file! ", ex.getLocalizedMessage(), ex, 0);
            }
        }
    }

    @Override
    public List<StorageTable> getInputTables() throws KBCException {

        if (this.sourceTables == null) {
            this.sourceTables = new ArrayList();
            if (config.getStorage().getInputTables().getTables().isEmpty()) {
                throw new KBCException("No input tables found. Have you specified input mapping?", "Input mapping must be specified!", null);
            }

            List<KBCOutputMapping> inputMp = config.getStorage().getInputTables().getTables();
            ManifestFile srcManifest;
            for (KBCOutputMapping t : inputMp) {
                File src;

                src = new File(this.inTablesPath + File.separator + t.getDestination());
                if (!src.exists()) {
                    throw new KBCException("Source file " + src.getName() + " specified in input mapping does not exist!", "Source file not found in input folder!", src, 2);
                }
                //parse manifest
                try {
                    srcManifest = ManifestParser.parseFile(new File(src.getAbsolutePath() + ".manifest"));
                    sourceTables.add(new StorageTable(srcManifest, src));
                } catch (IOException ex) {
                    throw new KBCException("Unable to parse manifest file " + src.getName() + ".manifest", ex.getMessage(), ex, 1);
                }
            }
        }

        return this.sourceTables;
    }

    @Override
    public IKBCParameters getParameters() {
        return this.config.getParams();
    }

    @Override
    public void validateConfig(File confFile) throws KBCException {

    }

    @Override
    public String getDataPath() {
        return dataPath;
    }

    @Override
    public String getOutputTablesPath() {
        return outputTablesPath;
    }

    @Override
    public String getInTablesPath() {
        return inTablesPath;
    }

    @Override
    public String getInPath() {
        return dataPath + File.separator + "in";
    }

    @Override
    public String getOutPath() {
        return dataPath + File.separator + "out";
    }

    @Override
    public LastState getStateFile() throws KBCException {
        if (this.lastState == null) {
            retrieveStateFile();
        }
        return this.lastState;
    }

    @Override
    public void writeManifestFile(ManifestFile manifest) throws KBCException {
        try {
            ManifestWriter.buildManifestFile(manifest, outputTablesPath);
        } catch (IOException ex) {
            throw new KBCException("Error writing manifest file for table: " + manifest.getName() + ". ", ex.getMessage(), ex);
        }
    }

    @Override
    public void writeStateFile(LastState stFile) throws KBCException {
        try {
            LastStateWriter.writeStateFile(getOutPath(), stFile, format);
        } catch (IOException ex) {
            throw new KBCException("Error writing state file.", ex.getMessage(), ex);
        }
    }

}
