/*
 * Copyright (c) 2022 StarTree Inc. All rights reserved.
 * Confidential and Proprietary Information of StarTree Inc.
 */

package org.apache.pinot.testcontainer;

import java.io.File;

public class AddTable {

    private File schemaFile;
    private File tableConfigFile;

    public AddTable(File schemaFile, File tableConfigFile) {
        this.schemaFile = schemaFile;
        this.tableConfigFile = tableConfigFile;
    }

    public File getSchemaFile() {
        return schemaFile;
    }

    public File getTableConfigFile() {
        return tableConfigFile;
    }
}
