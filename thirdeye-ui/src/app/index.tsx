import { CssBaseline, ThemeProvider } from "@material-ui/core";
import i18n from "i18next";
import numbro from "numbro";
import React, { StrictMode } from "react";
import ReactDOM from "react-dom";
import { initReactI18next } from "react-i18next";
import { BrowserRouter as Router } from "react-router-dom";
import { App } from "./app";
import { ApplicationSnackbarProvider } from "./components/application-snackbar-provider/application-snackbar-provider.component";
import "./index.scss";
import { enUs } from "./locale/numbers/en-us";
import { getInitOptions } from "./utils/i18next/i18next-util";
import { theme } from "./utils/material-ui/theme-util";

// Initialize localization
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
            <ApplicationSnackbarProvider>
                <Router>
                    {/* App needs to be rendered by a router to allow navigation using
                    ApplicationBar */}
                    <App />
                </Router>
            </ApplicationSnackbarProvider>
        </ThemeProvider>
    </StrictMode>,
    document.getElementById("root") as HTMLElement
);
