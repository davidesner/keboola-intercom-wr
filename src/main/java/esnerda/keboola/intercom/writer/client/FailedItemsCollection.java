/*
 */
package esnerda.keboola.intercom.writer.client;

import esnerda.keboola.intercom.writer.client.request.FailedBulkRequestItem;
import esnerda.keboola.intercom.writer.client.request.FailedUserBulkRequestItem;
import esnerda.keboola.intercom.writer.client.request.UserBulkJobRequest;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.supercsv.cellprocessor.Optional;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.prefs.CsvPreference;

/**
 *
 * author David Esner <esnerda at gmail.com>
 * created 2016
 */
public class FailedItemsCollection {

    private final List<FailedBulkRequestItem> items = new ArrayList<>();
    private final String type;

    public FailedItemsCollection(String type) {
        this.type = type;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void addAll(Collection<FailedBulkRequestItem> c) throws IllegalValueException {
        for (FailedBulkRequestItem f : c) {
            this.addItem(f);
        }
    }

    public void addAllBulkJob(UserBulkJobRequest c) throws IllegalValueException {
        if (!c.getItems().isEmpty()) {
            c.getItems().stream().forEach((i) -> {
                items.add(new FailedUserBulkRequestItem(i));

            });
        }
    }

    public void addItem(FailedBulkRequestItem item) throws IllegalValueException {
        if (!item.getType().equals(item.getType())) {
            throw new IllegalValueException("Cannot add item with type: " + item.getType() + "into collection type: " + this.type);
        }
        this.items.add(item);
    }

    public List<FailedBulkRequestItem> getItems() {
        return items;
    }

    /**
     * Saves failed items as CSV table.
     *
     * Returns File object containing the newly created file or null if
     * collection is empty.
     *
     * @param path
     * @return
     * @throws IOException
     */
    public File saveToCsv(String path) throws IOException {
        if (items.isEmpty()) {
            return null;
        }
        File errors = new File(path + File.separator + this.type + "_errors.csv");

//create all non exists folders            
        new File(errors.getParent()).mkdirs();

        try (CsvMapWriter mapWriter = new CsvMapWriter(new BufferedWriter(new FileWriter(errors)),
                CsvPreference.STANDARD_PREFERENCE)) {
            final String[] header = getAllHeaders().toArray(new String[0]);
            final CellProcessor[] processors = getProcessors(header.length);

            // write the header
            mapWriter.writeHeader(header);
            //write to file
            for (FailedBulkRequestItem it : items) {
                HashMap merged = new HashMap(it.getParametersMap());
                merged.putAll(it.getErrorMessages());
                mapWriter.write(merged, header, processors);

            }
        }
        return errors;

    }

    private List<String> getAllHeaders() {
        Set<String> headers = new HashSet<>();
        for (FailedBulkRequestItem it : items) {
            it.getParametersMap().keySet().stream().forEach((h) -> {
                headers.add((String) h);
            }
            );
            it.getErrorMessages().keySet().stream().forEach((h) -> {
                headers.add((String) h);
            });
        }
        return new ArrayList(headers);
    }

    /**
     * get cell processors with dynamic size
     *
     * @param length
     * @return
     */
    private static CellProcessor[] getProcessors(int length) {
        CellProcessor[] processors = new CellProcessor[length];
        for (int i = 0; i < length; i++) {
            processors[i] = new Optional();

        }

        return processors;
    }

    public class IllegalValueException extends Exception {

        public IllegalValueException(String message) {
            super(message);
        }

    }
}
