import { AppContainerV1 } from "@startree-ui/platform-ui";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { AppBar } from "./components/app-bar/app-bar.component";
import { AppRouter } from "./routers/app/app.router";

// ThirdEye UI app
export const App: FunctionComponent = () => {
    const { t } = useTranslation();

    return (
        <>
            <AppContainerV1 name={t("label.thirdeye")}>
                <AppBar />
                <AppRouter />
            </AppContainerV1>
        </>
    );
};
