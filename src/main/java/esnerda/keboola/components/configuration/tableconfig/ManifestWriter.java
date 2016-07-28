/*
 */
package esnerda.keboola.components.configuration.tableconfig;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

/**
 *
 * author David Esner <code>&lt;esnerda at gmail.com&gt;</code>
 * created 2016
 */
public class ManifestWriter {

    public static void buildManifestFile(ManifestFile file, String folderPath) throws IOException {
        /*Build manifest file*/
        File resFile = new File(folderPath + File.separator + file.getName() + ".manifest");
        ObjectMapper mapper = new ObjectMapper(new JsonFactory());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.writeValue(resFile, file);
    }
}
