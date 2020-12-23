import { CssBaseline, ThemeProvider } from "@material-ui/core";
import i18n from "i18next";
import numbro from "numbro";
import React, { StrictMode } from "react";
import ReactDOM from "react-dom";
import { initReactI18next } from "react-i18next";
import { Router } from "react-router-dom";
import { App } from "./app";
import { AppSnackbarProvider } from "./components/app-snackbar-provider/app-snackbar-provider.component";
import "./index.scss";
import { enUs } from "./locale/numbers/en-us";
import { appHistory } from "./utils/history-util/history-util";
import { getInitOptions } from "./utils/i18next-util/i18next-util";
import { theme } from "./utils/material-ui-util/theme-util";

// Initialize locale
// i18next (language)
i18n.use(initReactI18next).init(getInitOptions());
// Numbro (number formatting)
numbro.registerLanguage(enUs);
numbro.setLanguage("en-US");
// Luxon (date, time formatting), picks up system default

// Aplication entry point
ReactDOM.render(
    <StrictMode>
        {/* Apply Meterial UI theme */}
        <ThemeProvider theme={theme}>
            <CssBaseline />

            {/* Apply snackbar provider */}
            <AppSnackbarProvider>
                <Router history={appHistory}>
                    {/* App needs to be rendered by a router to allow navigation using app bar */}
                    <App />
                </Router>
            </AppSnackbarProvider>
        </ThemeProvider>
    </StrictMode>,
    document.getElementById("root") as HTMLElement
);
