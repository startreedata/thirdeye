import { CssBaseline, ThemeProvider } from "@material-ui/core";
import axios from "axios";
import i18n from "i18next";
import React, { FunctionComponent, useEffect, useState } from "react";
import { initReactI18next } from "react-i18next";
import { ApplicationBar } from "./components/application-bar/application-bar.component";
import { ApplicationRouter } from "./routers/application-router";
import { useAuthStore } from "./store/auth/auth-store";
import {
    getFulfilledResponseInterceptor,
    getRejectedResponseInterceptor,
    getRequestInterceptor,
} from "./utils/axios/axios-util";
import { getInitOptions } from "./utils/i18next/i18next-util";
import { theme } from "./utils/material-ui/theme-util";

// ThirdEye UI app
export const App: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [accessToken, removeAccessToken] = useAuthStore((state) => [
        state.accessToken,
        state.removeAccessToken,
    ]);

    useEffect(() => {
        // Application initializations
        // i18next
        i18n.use(initReactI18next).init(getInitOptions());
        // axios
        axios.interceptors.request.use(getRequestInterceptor(accessToken));
        axios.interceptors.response.use(
            getFulfilledResponseInterceptor(),
            getRejectedResponseInterceptor(removeAccessToken)
        );

        setLoading(false);
    }, [accessToken, removeAccessToken]);

    if (loading) {
        // Wait until application initializations complete
        return <></>;
    }

    return (
        // Apply Meterial UI theme
        <ThemeProvider theme={theme}>
            <CssBaseline />

            {/* Application bar */}
            <ApplicationBar />

            {/* Application router */}
            <ApplicationRouter />
        </ThemeProvider>
    );
};
