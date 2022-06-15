///
/// Copyright 2022 StarTree Inc
///
/// Licensed under the StarTree Community License (the "License"); you may not use
/// this file except in compliance with the License. You may obtain a copy of the
/// License at http://www.startree.ai/legal/startree-community-license
///
/// Unless required by applicable law or agreed to in writing, software distributed under the
/// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
/// either express or implied.
/// See the License for the specific language governing permissions and limitations under
/// the License.
///

import axios from "axios";
import { LogicalMetric, Metric } from "../dto/metric.interfaces";

const BASE_URL_METRICS = "/api/metrics";

export const getMetric = async (id: number): Promise<Metric> => {
    const response = await axios.get(`${BASE_URL_METRICS}/${id}`);

    return response.data;
};

export const getAllMetrics = async (): Promise<Metric[]> => {
    const response = await axios.get(BASE_URL_METRICS);

    return response.data;
};

export const createMetric = async (metric: LogicalMetric): Promise<Metric> => {
    const response = await axios.post(BASE_URL_METRICS, [metric]);

    return response.data[0];
};

export const updateMetric = async (metric: LogicalMetric): Promise<Metric> => {
    const response = await axios.put(BASE_URL_METRICS, [metric]);

    return response.data[0];
};

export const deleteMetric = async (id: number): Promise<Metric> => {
    const response = await axios.delete(`${BASE_URL_METRICS}/${id}`);

    return response.data;
};
