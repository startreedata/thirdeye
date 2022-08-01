/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.pinot.testcontainer;

import java.io.File;

public class ImportData {

    private final File batchJobSpecFile;
    private final File dataFile;

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
