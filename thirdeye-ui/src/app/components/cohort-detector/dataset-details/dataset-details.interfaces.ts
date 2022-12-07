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
import { Dataset } from "../../../rest/dto/dataset.interfaces";
import { MetricAggFunction } from "../../../rest/dto/metric.interfaces";
import { GetCohortParams } from "../../../rest/rca/rca.interfaces";

export interface DatasetDetailsProps {
    onSearchButtonClick: (getCohortsParams: GetCohortParams) => void;
    title: string;
    subtitle?: string;
    submitButtonLabel: string;
    onMetricSelect?: (
        metric: string,
        dataset: Dataset,
        aggregationFunction: string
    ) => void;
    onAggregationFunctionSelect?: (
        aggregationFunction: MetricAggFunction
    ) => void;
}
