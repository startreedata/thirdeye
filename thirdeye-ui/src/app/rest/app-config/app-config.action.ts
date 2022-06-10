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


import { useHTTPAction } from "../create-rest-action";
import { AppConfiguration } from "../dto/app-config.interface";
import { GetAppConfiguration } from "./app-config.interfaces";
import { getAppConfiguration as getAppConfigurationRest } from "./app-config.rest";

export const useGetAppConfiguration = (): GetAppConfiguration => {
    const { data, makeRequest, status, errorMessages } =
        useHTTPAction<AppConfiguration>(getAppConfigurationRest);

    const getAppConfiguration = (): Promise<AppConfiguration | undefined> => {
        return makeRequest();
    };

    return { appConfig: data, getAppConfiguration, status, errorMessages };
};
