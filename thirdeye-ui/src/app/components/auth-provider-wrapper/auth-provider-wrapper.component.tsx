import { isNull } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import {
    AppLoadingIndicatorV1,
    AuthProviderV1,
    AuthRedirectMethodV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetAppConfiguration } from "../../rest/app-config/app-config.action";
import { AppConfiguration } from "../../rest/dto/app-config.interface";
import { AppRoute } from "../../utils/routes/routes.util";
import { AuthProviderWrapperProps } from "./auth-provider-wrapper.interfaces";

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
        if (isNull(clientId)) {
            // Auth data yet to be gathered
            return;
        }

        if (!clientId) {
            // Auth data missing
            setAppInitFailure(true);

            return;
        }

        setAppInitFailure(false);
    }, [clientId]);

    useEffect(() => {
        if (isNull(appInitFailure)) {
            // Auth data yet to be processed
            return;
        }

        setLoading(false);
    }, [appInitFailure]);

    useEffect(() => {
        if (getAppConfigurationStatus === ActionStatus.Done) {
            gatherAuthData(fetchedAppConfiguration);
        }

        if (getAppConfigurationStatus === ActionStatus.Error) {
            gatherAuthData(null);
        }
    }, [getAppConfigurationStatus]);

    const gatherAuthData = (
        appConfiguration: AppConfiguration | null
    ): void => {
        // Validate received data
        if (!appConfiguration || !appConfiguration.clientId) {
            setClientId("");

            return;
        }

        setClientId(appConfiguration.clientId);
    };

    // Loading indicator
    if (loading) {
        return <AppLoadingIndicatorV1 />;
    }

    return (
        <AuthProviderV1
            appInitFailure={appInitFailure as boolean}
            clientId={clientId as string}
            oidcIssuerLogoutPath="/v2/logout"
            redirectMethod={AuthRedirectMethodV1.Post}
            redirectPathBlacklist={[AppRoute.LOGIN, AppRoute.LOGOUT]}
        >
            {children}
        </AuthProviderV1>
    );
};
