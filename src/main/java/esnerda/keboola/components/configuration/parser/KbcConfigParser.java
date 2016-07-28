/*
 */
package esnerda.keboola.components.configuration.parser;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import esnerda.keboola.components.configuration.ConfigFormat;
import esnerda.keboola.components.configuration.IKBCParameters;
import esnerda.keboola.components.configuration.KBCConfig;
import java.io.File;
import java.io.IOException;

/**
 *
 * author David Esner <code>&lt;esnerda at gmail.com&gt;</code>
 * created 2015
 */
public class KbcConfigParser implements ConfigParser {

    private ObjectMapper mapper;
    private String format;

    public KbcConfigParser(String format) throws Exception {
        try {
            ConfigFormat.valueOf(format);
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid format");
        }
        switch (ConfigFormat.valueOf(format)) {
            case json:
                this.mapper = new ObjectMapper(new JsonFactory());
                this.format = format;
                break;
            case yml:
                this.mapper = new ObjectMapper(new YAMLFactory());
                this.format = format;
                break;
            default:
                this.mapper = new ObjectMapper(new JsonFactory());
                this.format = format;
                break;
        }
    }

    @Override
    public KBCConfig parseConfigFile(File file, Class paramsType) throws IOException {

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        SimpleModule module = new SimpleModule("KBCParamsTyping", Version.unknownVersion());
        module.addAbstractTypeMapping(IKBCParameters.class, paramsType);
        mapper.registerModule(module); // important, otherwise won't have any effect on mapper's configuration
        mapper.findAndRegisterModules();
        return (KBCConfig) mapper.readValue(file, KBCConfig.class);
    }

    @Override
    public Object parseFile(File file, Class type) throws IOException {

        mapper.findAndRegisterModules();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(file, type);
    }

    @Override
    public void setFormat(String format) throws Exception {
        try {
            ConfigFormat.valueOf(format);
        } catch (IllegalArgumentException e) {
            throw new Exception("Invalid format");
        }
        switch (ConfigFormat.valueOf(format)) {
            case json:
                this.mapper = new ObjectMapper(new JsonFactory());
                break;
            case yml:
                this.mapper = new ObjectMapper(new YAMLFactory());
                break;
            default:
                this.mapper = new ObjectMapper(new JsonFactory());
                break;
        }
        this.format = format;
    }

    @Override
    public String getFormat() {
        return this.format;
    }
}
