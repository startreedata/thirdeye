import { CssBaseline, ThemeProvider } from "@material-ui/core";
import {
    AuthProviderV1,
    AuthRedirectMethodV1,
    lightV1,
    NotificationProviderV1,
} from "@startree-ui/platform-ui";
import "@startree-ui/platform-ui/assets/styles/fonts.scss";
import "@startree-ui/platform-ui/assets/styles/layout.scss";
import { enableAllPlugins } from "immer";
import React, { StrictMode } from "react";
import ReactDOM from "react-dom";
import { Router } from "react-router-dom";
import { App } from "./app";
import { AppBreadcrumbsProvider } from "./components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { DialogProvider } from "./components/dialogs/dialog-provider/dialog-provider.component";
import { SnackbarProvider } from "./components/snackbar-provider/snackbar-provider.component";
import { TimeRangeProvider } from "./components/time-range/time-range-provider/time-range-provider.component";
import { appHistory } from "./utils/history/history.util";
import { initLocale } from "./utils/locale/locale.util";
import { AppRoute } from "./utils/routes/routes.util";
import { getClientIdFromUrl } from "./utils/url/client-id.util";

// Initialize locale
initLocale();

// Initialize Immer to handle maps and sets
enableAllPlugins();

// App entry point
ReactDOM.render(
    <StrictMode>
        {/* Material-UI theme */}
        <ThemeProvider theme={lightV1}>
            <CssBaseline />

            {/* App rendered by a router to allow navigation using app bar */}
            <Router history={appHistory}>
                <SnackbarProvider>
                    <NotificationProviderV1>
                        <AuthProviderV1
                            clientId={
                                getClientIdFromUrl(window.location.href) || ""
                            }
                            redirectMethod={AuthRedirectMethodV1.Post}
                            redirectPathBlacklist={[
                                AppRoute.LOGIN,
                                AppRoute.LOGOUT,
                            ]}
                        >
                            <TimeRangeProvider>
                                <AppBreadcrumbsProvider>
                                    <DialogProvider>
                                        <App />
                                    </DialogProvider>
                                </AppBreadcrumbsProvider>
                            </TimeRangeProvider>
                        </AuthProviderV1>
                    </NotificationProviderV1>
                </SnackbarProvider>
            </Router>
        </ThemeProvider>
    </StrictMode>,
    document.getElementById("root") as HTMLElement
);
