import {
    AppContainerV1,
    NotificationScopeV1,
    NotificationTypeV1,
    useAuthProviderV1,
    useNotificationProviderV1,
} from "@startree-ui/platform-ui";
import { delay } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { AppBar } from "./components/app-bar/app-bar.component";
import { AppRouter } from "./routers/app/app.router";

// ThirdEye UI app
export const App: FunctionComponent = () => {
    const [showAppNavBar, setShowAppNavBar] = useState(false);
    const {
        authDisabled,
        authDisabledNotification,
        clearAuthDisabledNotification,
    } = useAuthProviderV1();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    useEffect(() => {
        // Slight delay in rendering nav bar helps avoid flicker during initial page redirects
        delay(setShowAppNavBar, 200, true);
    }, []);

    useEffect(() => {
        if (authDisabled && authDisabledNotification) {
            // Auth disabled and user not yet notified
            notify(
                NotificationTypeV1.Info,
                t("message.authentication-disabled"),
                false,
                NotificationScopeV1.Global,
                clearAuthDisabledNotification
            );
        }
    }, [authDisabled, authDisabledNotification]);

    return (
        <AppContainerV1 name={t("label.thirdeye")}>
            {showAppNavBar && <AppBar />}
            <AppRouter />
        </AppContainerV1>
    );
};
