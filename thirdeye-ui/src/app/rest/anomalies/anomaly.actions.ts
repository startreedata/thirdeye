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
import { Anomaly } from "../dto/anomaly.interfaces";
import {
    getAnomalies as getAnomaliesRest,
    getAnomaly as getAnomalyRest,
} from "./anomalies.rest";
import {
    GetAnomalies,
    GetAnomaliesProps,
    GetAnomaly,
} from "./anomaly.interfaces";

export const useGetAnomaly = (): GetAnomaly => {
    const { data, makeRequest, status, errorMessages } =
        useHTTPAction<Anomaly>(getAnomalyRest);

    const getAnomaly = (id: number): Promise<Anomaly | undefined> => {
        return makeRequest(id);
    };

    return { anomaly: data, getAnomaly, status, errorMessages };
};

export const useGetAnomalies = (): GetAnomalies => {
    const { data, makeRequest, status, errorMessages } =
        useHTTPAction<Anomaly[]>(getAnomaliesRest);

    const getAnomalies = (
        getAnomalyParams: GetAnomaliesProps = {}
    ): Promise<Anomaly[] | undefined> => {
        return makeRequest(getAnomalyParams);
    };

    return {
        anomalies: data,
        getAnomalies,
        status,
        errorMessages,
    };
};
