/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { UiDataset } from "../../rest/dto/ui-dataset.interfaces";

export interface DatasetListV1Props {
    datasets: UiDataset[] | null;
    onDelete?: (uiDatasets: UiDataset[]) => void;
}

export const TEST_IDS = {
    TABLE: "dataset-list-table",
    EDIT_BUTTON: "dataset-list-edit-button",
    DELETE_BUTTON: "dataset-list-delete-button",
};