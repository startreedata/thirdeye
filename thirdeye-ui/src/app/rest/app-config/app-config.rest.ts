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
import axios from "axios";
import { AppConfiguration } from "../dto/app-config.interface";

const URL_CONFIG = "/api/ui/config";

export const getAppConfiguration = async (): Promise<AppConfiguration> => {
    if (VERCEL_DEPLOYMENT_API_URL) {
        axios.defaults.baseURL = VERCEL_DEPLOYMENT_API_URL;
    }
    const response = await axios.get(URL_CONFIG);

    return response.data;
};
