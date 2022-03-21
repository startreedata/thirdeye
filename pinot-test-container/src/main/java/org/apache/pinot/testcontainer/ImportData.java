package org.apache.pinot.testcontainer;

import java.io.File;

public class ImportData {

    private File batchJobSpecFile;
    private File dataFile;

    public ImportData(File batchJobSpecFile, File dataFile) {
        this.batchJobSpecFile = batchJobSpecFile;
        this.dataFile = dataFile;
    }

    public File getBatchJobSpecFile() {
        return batchJobSpecFile;
    }

    public File getDataFile() {
        return dataFile;
    }
}
