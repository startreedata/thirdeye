import { CssBaseline, ThemeProvider } from "@material-ui/core";
import { enableAllPlugins } from "immer";
import React, { StrictMode } from "react";
import ReactDOM from "react-dom";
import { Router } from "react-router-dom";
import { App } from "./app";
import { AuthProvider } from "./components/auth-provider/auth-provider.component";
import { DialogProvider } from "./components/dialogs/dialog-provider/dialog-provider.component";
import { SnackbarProvider } from "./components/snackbar-provider/snackbar-provider.component";
import { TimeRangeProvider } from "./components/time-range/time-range-provider/time-range-provider.component";
import "./index.scss";
import { appHistory } from "./utils/history/history.util";
import { initLocale } from "./utils/locale/locale.util";
import { theme } from "./utils/material-ui/theme.util";

// Initialize locale
initLocale();

// Initialize Immer to handle maps and sets
enableAllPlugins();

// App entry point
ReactDOM.render(
    <StrictMode>
        {/* Material-UI theme */}
        <ThemeProvider theme={theme}>
            <CssBaseline />

            {/* App rendered by a router to allow navigation using app bar */}
            <Router history={appHistory}>
                <SnackbarProvider>
                    <AuthProvider>
                        <TimeRangeProvider>
                            <DialogProvider>
                                <App />
                            </DialogProvider>
                        </TimeRangeProvider>
                    </AuthProvider>
                </SnackbarProvider>
            </Router>
        </ThemeProvider>
    </StrictMode>,
    document.getElementById("root") as HTMLElement
);
