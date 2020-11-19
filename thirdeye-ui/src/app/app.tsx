import { CssBaseline, ThemeProvider } from "@material-ui/core";
import axios from "axios";
import i18n from "i18next";
import React, { FunctionComponent } from "react";
import { initReactI18next } from "react-i18next";
import { ApplicationBar } from "./components/application-bar/application-bar.component";
import { AppRouter } from "./routers/app-router";
import { requestInterceptor } from "./utils/axios/axios.util";
import { getInitOptions } from "./utils/i18next/i18next.util";
import { theme } from "./utils/material-ui/theme.util";

// Initializations
// i18next
i18n.use(initReactI18next).init(getInitOptions());
// axios
axios.interceptors.request.use(requestInterceptor);

// ThirdEye UI app
export const App: FunctionComponent = () => {
    return (
        // Apply Meterial UI theme
        <ThemeProvider theme={theme}>
            <CssBaseline />

            {/* Application bar */}
            <ApplicationBar />

            {/* Router */}
            <AppRouter />
        </ThemeProvider>
    );
};
