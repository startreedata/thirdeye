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
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */

import type { CheckboxProps } from "@material-ui/core";
import type { Dispatch, SetStateAction } from "react";
import type { Dataset } from "../../rest/dto/dataset.interfaces";

// TODO: Remove if not needed
export interface DatasetOption {
    label: string;
    value: number | string;
}

export interface WelcomeSelectDatasetOutletContext {
    selectedDatasource: number;
    selectedDatasets: Dataset[];
    setSelectedDatasets: Dispatch<SetStateAction<Dataset[]>>;
}

export interface SelectDatasetProps {
    checked: boolean;
    indeterminate?: boolean;
    onChange: CheckboxProps["onChange"];
    labelPrimaryText: string;
    labelSecondaryText?: string;
    name: string;
}
