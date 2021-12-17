import { AppContainerV1, useAuthProviderV1 } from "@startree-ui/platform-ui";
import { delay } from "lodash";
import { useSnackbar } from "notistack";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { AppBar } from "./components/app-bar/app-bar.component";
import { AppRouter } from "./routers/app/app.router";

// ThirdEye UI app
export const App: FunctionComponent = () => {
    const [showAppNavBar, setShowAppNavBar] = useState(false);
    const { authDisabled, authDisabledNotification } = useAuthProviderV1();
    const { t } = useTranslation();
    const { enqueueSnackbar } = useSnackbar();

    useEffect(() => {
        // Slight delay in rendering nav bar helps avoid flicker during initial page redirects
        delay(setShowAppNavBar, 200, true);
    }, []);

    useEffect(() => {
        if (authDisabled && authDisabledNotification) {
            // Auth disabled and user not yet notified
            enqueueSnackbar(t("message.authentication-disabled"));
        }
    }, [authDisabled, authDisabledNotification]);

    return (
        <AppContainerV1 name={t("label.thirdeye")}>
            {showAppNavBar && <AppBar />}
            <AppRouter />
        </AppContainerV1>
    );
};
