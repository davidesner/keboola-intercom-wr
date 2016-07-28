/*
 */
package esnerda.keboola.components.configuration.parser;

import esnerda.keboola.components.configuration.KBCConfig;
import java.io.File;
import java.io.IOException;

/**
 *
 * author David Esner <code>&lt;esnerda at gmail.com&gt;</code>
 * created 2016
 */
public interface ConfigParser {

    public void setFormat(String format) throws Exception;

    public String getFormat();

    public KBCConfig parseConfigFile(File file, Class paramsType) throws IOException;

    public Object parseFile(File file, Class type) throws IOException;
}
