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

import { EditableAlert } from "../../rest/dto/alert.interfaces";

export const createAlertConfigForInsights = (
    datasource: string,
    dataset: string,
    metric: string,
    aggregationFunction: string
): EditableAlert => {
    return {
        name: "",
        description: "",
        cron: "0 0 5 ? * MON-FRI *",
        template: { name: "startree-mean-variance" },
        templateProperties: {
            dataSource: datasource,
            dataset: dataset,
            aggregationColumn: metric,
            aggregationFunction: aggregationFunction,
            seasonalityPeriod: "P7D",
            lookback: "P90D",
            monitoringGranularity: "P1D",
            sensitivity: "3",
            timezone: "UTC",
        },
    };
};
