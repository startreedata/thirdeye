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
import axios from "axios";
import { filterOutIgnoredAnomalies } from "../../utils/anomalies/anomalies.util";
import { Anomaly, AnomalyFeedback } from "../dto/anomaly.interfaces";
import { GetAnomaliesProps } from "./anomaly.interfaces";

const BASE_URL_ANOMALIES = "/api/anomalies";

export const getAnomaly = async (id: number): Promise<Anomaly> => {
    const response = await axios.get(`${BASE_URL_ANOMALIES}/${id}`);

    return response.data;
};

export const getAnomalies = async ({
    alertId,
    startTime,
    endTime,
    dataset,
    metric,
    enumerationItemId,
    showAllAnomalies = false,
}: GetAnomaliesProps = {}): Promise<Anomaly[]> => {
    const queryParams = new URLSearchParams([["isChild", "false"]]);

    if (alertId) {
        queryParams.set("alert.id", alertId.toString());
    }

    if (startTime) {
        queryParams.set("startTime", `[gte]${startTime}`);
    }

    if (endTime) {
        queryParams.set("endTime", `[lte]${endTime}`);
    }

    if (dataset) {
        queryParams.set("metadata.dataset.name", dataset);
    }

    if (metric) {
        queryParams.set("metadata.metric.name", metric);
    }

    if (enumerationItemId) {
        queryParams.set("enumerationItem.id", enumerationItemId.toString());
    }

    const response = await axios.get<Anomaly[]>(
        `${BASE_URL_ANOMALIES}?${queryParams.toString()}`
    );

    return showAllAnomalies
        ? response.data
        : filterOutIgnoredAnomalies(response.data);
};

export const deleteAnomaly = async (id: number): Promise<Anomaly> => {
    const response = await axios.delete(`${BASE_URL_ANOMALIES}/${id}`);

    return response.data;
};

export const updateAnomalyFeedback = async (
    anomalyId: number,
    feedback: AnomalyFeedback
): Promise<AnomalyFeedback> => {
    const response = await axios.post(
        `${BASE_URL_ANOMALIES}/${anomalyId}/feedback`,
        feedback
    );

    return response.data;
};
