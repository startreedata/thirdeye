import { isNull } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import {
    AppLoadingIndicatorV1,
    AuthProviderV1,
    AuthRedirectMethodV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetAppConfiguration } from "../../rest/app-config/app-config.action";
import { AppRoute } from "../../utils/routes/routes.util";
import { AuthProviderWrapperProps } from "./auth-provider-wrapper.interfaces";
import { isAuthDisabled, processAuthData } from "./auth-provider-wrapper.utils";

export const AuthProviderWrapper: FunctionComponent<
    AuthProviderWrapperProps
> = ({ children }) => {
    const [loading, setLoading] = useState(true);
    const [clientId, setClientId] = useState<string | null>(null);
    const [appInitFailure, setAppInitFailure] = useState<boolean | null>(null);
    const {
        appConfig: fetchedAppConfiguration,
        getAppConfiguration,
        status: getAppConfigurationStatus,
    } = useGetAppConfiguration();

    useEffect(() => {
        getAppConfiguration();
    }, []);

    useEffect(() => {
        if (isNull(appInitFailure)) {
            // Auth data yet to be processed
            return;
        }

        setLoading(false);
    }, [appInitFailure]);

    useEffect(() => {
        if (getAppConfigurationStatus === ActionStatus.Done) {
            setClientId(processAuthData(fetchedAppConfiguration));
            setAppInitFailure(false);
        }

        if (getAppConfigurationStatus === ActionStatus.Error) {
            setClientId(processAuthData(null));
            setAppInitFailure(true);
        }
    }, [getAppConfigurationStatus]);

    // Loading indicator
    if (loading) {
        return <AppLoadingIndicatorV1 />;
    }

    return (
        <AuthProviderV1
            appInitFailure={appInitFailure as boolean}
            clientId={clientId as string}
            disableAuthOverride={isAuthDisabled(fetchedAppConfiguration)}
            redirectMethod={AuthRedirectMethodV1.Post}
            redirectPathBlacklist={[AppRoute.LOGIN, AppRoute.LOGOUT]}
        >
            {children}
        </AuthProviderV1>
    );
};
