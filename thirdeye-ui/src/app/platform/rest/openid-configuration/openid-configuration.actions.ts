// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
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
