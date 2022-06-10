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

import { AppConfiguration } from "../../rest/dto/app-config.interface";

export const DUMMY_CLIENT_ID = "dummy-client-id";

export const processAuthData = (
    appConfiguration: AppConfiguration | null
): string => {
    // If auth is disabled, and client id is missing,  set a dummy client id
    if (isAuthDisabled(appConfiguration)) {
        if (!(appConfiguration as AppConfiguration).clientId) {
            return DUMMY_CLIENT_ID;
        }
    }

    // Validate received data
    if (!appConfiguration || !appConfiguration.clientId) {
        return "";
    }

    return appConfiguration.clientId;
};

export const isAuthDisabled = (
    appConfiguration: AppConfiguration | null
): boolean => {
    return (
        appConfiguration !== null &&
        appConfiguration.authEnabled !== undefined &&
        !appConfiguration.authEnabled
    );
};
