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
import type { AppAnalytics } from "../dto/app-analytics.interfaces";
import type { AppAnalyticsProps } from "./app-analytics.interfaces";

const URL_CONFIG = "/api/app-analytics";

export const getAppAnalytics = async (
    params?: AppAnalyticsProps
): Promise<AppAnalytics> => {
    const queryParams = new URLSearchParams([]);

    const { startTime, endTime } = params || {};

    if (startTime) {
        queryParams.set("startTime", `[gte]${startTime}`);
    }

    if (endTime) {
        queryParams.set("endTime", `[lte]${endTime}`);
    }

    const response = await axios.get<AppAnalytics>(
        `${URL_CONFIG}?${queryParams.toString()}`
    );

    return response.data;
};
