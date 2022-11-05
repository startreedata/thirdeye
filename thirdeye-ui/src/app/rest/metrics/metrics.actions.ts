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
import { useHTTPAction } from "../create-rest-action";
import { Metric } from "../dto/metric.interfaces";
import { GetAllMetricsParams, GetMetrics } from "./metrics.interface";
import { getAllMetrics } from "./metrics.rest";

export const useGetMetrics = (): GetMetrics => {
    const { data, makeRequest, status, errorMessages } =
        useHTTPAction<Metric[]>(getAllMetrics);

    const getMetrics = (
        params: GetAllMetricsParams = {}
    ): Promise<Metric[] | undefined> => {
        return makeRequest(params);
    };

    return { metrics: data, getMetrics, status, errorMessages };
};
