// /
// / Copyright 2022 StarTree Inc
// /
// / Licensed under the StarTree Community License (the "License"); you may not use
// / this file except in compliance with the License. You may obtain a copy of the
// / License at http://www.startree.ai/legal/startree-community-license
// /
// / Unless required by applicable law or agreed to in writing, software distributed under the
// / License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// / either express or implied.
// / See the License for the specific language governing permissions and limitations under
// / the License.
// /

import { Dataset } from "./dataset.interfaces";

export interface LogicalMetric {
    id?: number;
    name: string;
    dataset?: Dataset;
    active: boolean;
    aggregationFunction: MetricAggFunction;
    rollupThreshold: number;
    updated?: number;
}

export interface Metric {
    id: number;
    name: string;
    urn: string;
    dataset: Dataset;
    active: boolean;
    created: number;
    updated: number;
    derived: boolean;
    derivedMetricExpression: string;
    aggregationColumn: string;
    aggregationFunction: MetricAggFunction;
    rollupThreshold: number;
    views: LogicalView[];
    where: string;
}
export interface LogicalView {
    name: string;
    query: string;
}

export enum MetricAggFunction {
    SUM = "SUM",
    AVG = "AVG",
    COUNT = "COUNT",
    COUNT_DISTINCT = "COUNT_DISTINCT",
    MAX = "MAX",
    PCT50 = "PCT50",
    PCT90 = "PCT90",
    PCT95 = "PCT95",
    PCT99 = "PCT99",
}
