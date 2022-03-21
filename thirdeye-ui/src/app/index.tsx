import { CssBaseline, ThemeProvider } from "@material-ui/core";
import { enableAllPlugins } from "immer";
import React, { StrictMode } from "react";
import ReactDOM from "react-dom";
import { BrowserRouter } from "react-router-dom";
import { App } from "./app";
import { DialogProvider } from "./components/dialogs/dialog-provider/dialog-provider.component";
import { TimeRangeProvider } from "./components/time-range/time-range-provider/time-range-provider.component";
import "./platform/assets/styles/fonts.scss";
import "./platform/assets/styles/layout.scss";
import {
    AuthProviderV1,
    AuthRedirectMethodV1,
    NotificationProviderV1,
} from "./platform/components";
import { lightV1 } from "./platform/utils";
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
            <BrowserRouter>
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
                            <DialogProvider>
                                <App />
                            </DialogProvider>
                        </TimeRangeProvider>
                    </AuthProviderV1>
                </NotificationProviderV1>
            </BrowserRouter>
        </ThemeProvider>
    </StrictMode>,
    document.getElementById("root") as HTMLElement
);
