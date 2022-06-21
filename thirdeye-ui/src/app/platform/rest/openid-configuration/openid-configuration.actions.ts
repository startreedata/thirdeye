/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { AxiosError } from "axios";
import { useCallback, useState } from "react";
import { ActionStatus } from "../actions.interfaces";
import { OpenIDConfigurationV1 } from "../dto/openid-configuration.interfaces";
import { GetOpenIDConfigurationV1 } from "./openid-configuration.interfaces";
import { getOpenIDConfigurationV1 as getOpenIDConfigurationV1REST } from "./openid-configuration.rest";

export const useGetOpenIDConfigurationV1 = (): GetOpenIDConfigurationV1 => {
    const [openIDConfigurationV1, setOpenIDConfigurationV1] =
        useState<OpenIDConfigurationV1 | null>(null);
    const [status, setStatus] = useState(ActionStatus.Initial);
    const [errorMessage, setErrorMessage] = useState("");

    const getOpenIDConfigurationV1 = useCallback(
        async (oidcIssuerUrl: string) => {
            setStatus(ActionStatus.Working);
            try {
                const fetchedOpenIDConfigurationV1 =
                    await getOpenIDConfigurationV1REST(oidcIssuerUrl);
                setOpenIDConfigurationV1(fetchedOpenIDConfigurationV1);
                setStatus(ActionStatus.Done);
                setErrorMessage("");
            } catch (error) {
                const axiosError = error as AxiosError;
                setOpenIDConfigurationV1(null);
                setStatus(ActionStatus.Error);
                setErrorMessage(
                    axiosError &&
                        axiosError.response &&
                        axiosError.response.data &&
                        axiosError.response.data.message
                );
            }
        },
        []
    );

    return {
        openIDConfigurationV1,
        getOpenIDConfigurationV1,
        status,
        errorMessage,
    };
};
