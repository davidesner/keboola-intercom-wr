/*
 */
package esnerda.keboola.components.configuration.handler;

import esnerda.keboola.components.KBCException;
import esnerda.keboola.components.appstate.LastState;
import esnerda.keboola.components.configuration.ConfigFormat;
import esnerda.keboola.components.configuration.IKBCParameters;
import esnerda.keboola.components.configuration.parser.KbcConfigParser;

/**
 *
 * author David Esner <code>&lt;esnerda at gmail.com&gt;</code>
 * created 2016
 */
public class ConfigHandlerBuilder {

    private boolean checkInputTables;
    private Class<? extends LastState> statefileType = null;
    private final Class<? extends IKBCParameters> parametersType;
    ConfigFormat format = null;

    public static ConfigHandlerBuilder create(Class<? extends IKBCParameters> parametersType) {
        return new ConfigHandlerBuilder(parametersType);
    }

    /**
     * Creates ConfigHandler with default parameters. Sets the environment
     * without input tables and without state file. Expect default json format.
     *
     * @param parametersType
     * @return
     */
    public static ConfigHandlerBuilder createDefault(Class<? extends IKBCParameters> parametersType) {
        ConfigHandlerBuilder c = new ConfigHandlerBuilder(parametersType).
                hasInputTables(false)
                .setFormat(ConfigFormat.json)
                .setStateFileType(null);

        return c;
    }

    /**
     * Create ConfigHandlerBuilder with specified IKBCParameters implementation.
     *
     * @param pType
     */
    private ConfigHandlerBuilder(Class<? extends IKBCParameters> pType) {
        this.parametersType = pType;
    }

    /**
     * If set to true, the Handler expects InputTables mapping and checks for
     * its existence during configuration processing.
     *
     * @param value
     * @return
     */
    public ConfigHandlerBuilder hasInputTables(boolean value) {
        this.checkInputTables = value;
        return this;
    }

    /**
     * Set the expected of a config file. The available formats are specified in
     * ConfigFormat enum (yaml,json).
     * The specified format is expected in a configuration file as well as in a
     * State file, if specified.
     *
     * DEFAULT: ConfigFormat.json
     *
     * @param f
     * @return
     */
    public ConfigHandlerBuilder setFormat(ConfigFormat f) {
        this.format = f;
        return this;
    }

    /**
     * Set the LastState implementation. This implementation will be expected
     * when parsing config file. ConfigHandler will automatically check for
     * state file existence.
     *
     * @param sType - IKBCParaneters implementation type.
     * @return
     */
    public ConfigHandlerBuilder setStateFileType(Class<? extends LastState> sType) {
        this.statefileType = sType;
        return this;
    }

    public KBCConfigurationEnvHandler build() throws KBCException {
        if (format == null) {
            format = ConfigFormat.json;
        }

        try {
            return new KBCConfigurationHandlerImpl(checkInputTables, statefileType, parametersType, new KbcConfigParser(format.name()), format);
        } catch (Exception ex) {
            throw new KBCException(ex.getMessage(), "", ex);
        }
    }

}
