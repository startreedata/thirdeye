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

import { EditableAlert } from "../../../rest/dto/alert.interfaces";

export interface SampleAlertSelectionProps {
    onSampleAlertSelect: (sampleAlertOption: SampleAlertOption) => void;
    basicAlertOptions: SampleAlertOption[];
    multiDimensionAlertOptions: SampleAlertOption[];
}

export interface SampleAlertOption {
    title: string;
    description: string;
    alertConfiguration: EditableAlert;
    recipeLink?: string;
    isDimensionExploration: boolean;
}

export const QUERY_PARAM_KEY_FOR_SAMPLE_ALERT_FILTER = "sampleAlertType";

export const SAMPLE_ALERT_TYPES = {
    MULTIDIMENSION: "multiDimension",
    BASIC: "basic",
};
