/*
 * Copyright 2024 StarTree Inc
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
// utils
import {
    GranularityValue,
    generateTemplateProperties,
} from "../../components/alert-wizard-v3/select-metric/select-metric.utils";

// types
import { EditableAlert } from "../../rest/dto/alert.interfaces";
import { Dataset } from "../../rest/dto/dataset.interfaces";
import { EnumerationItemParams } from "../../rest/dto/detection.interfaces";

const DEFAULT_CRON = "0 0 5 ? * MON-FRI *";

export const defaultStartingAlert: EditableAlert = {
    name: "",
    description: "",
    cron: DEFAULT_CRON,
    template: {
        name: "startree-threshold",
    },
    templateProperties: {
        dataSource: "sample_datasource",
        dataset: "sample_dataset",
        aggregationColumn: "sample_column_name",
        aggregationFunction: "SUM",
    },
};

type GenerateWorkingAlert = {
    dataset: Dataset;
    metric: string;
    aggregationFunction: string;
    templateName: string | undefined;
    granularity: GranularityValue;
    min?: number;
    max?: number;
    queryFilters?: string;
    dxAlertProps?: {
        queryFilters?: string;
        enumeratorQuery?: string;
        enumerationItems?: EnumerationItemParams[] | any;
    };
    isMultiDimensionAlert: boolean;
};

export const getWorkingAlert = (
    alertProps: GenerateWorkingAlert
): Partial<EditableAlert> => {
    const workingAlert: Partial<EditableAlert> = {
        ...defaultStartingAlert,
        template: {
            name: alertProps.templateName,
        },
        templateProperties: {
            ...defaultStartingAlert.templateProperties,
            ...generateTemplateProperties(
                alertProps.metric,
                alertProps.dataset,
                alertProps.aggregationFunction || "",
                alertProps.granularity
            ),
            queryFilters: alertProps.queryFilters || "",
        },
    };

    if (alertProps.min === 0) {
        workingAlert.templateProperties!.min = alertProps.min;
    }
    if (alertProps.max) {
        workingAlert.templateProperties!.max = alertProps.max;
    }

    if (alertProps.isMultiDimensionAlert) {
        const { queryFilters, enumeratorQuery, enumerationItems } =
            alertProps.dxAlertProps || {};

        workingAlert.templateProperties!.queryFilters = queryFilters
            ? "${queryFilters} " + `${queryFilters}`
            : "";

        if (enumeratorQuery) {
            workingAlert.templateProperties!.enumeratorQuery = enumeratorQuery;
        }
        if (enumerationItems) {
            workingAlert.templateProperties!.enumerationItems =
                enumerationItems;
        }
    }

    return workingAlert;
};
