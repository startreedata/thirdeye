import { CssBaseline, ThemeProvider } from "@material-ui/core";
import { enableAllPlugins } from "immer";
import React, { StrictMode } from "react";
import ReactDOM from "react-dom";
import { BrowserRouter } from "react-router-dom";
import { App } from "./app";
import { AuthProviderWrapper } from "./components/auth-provider-wrapper/auth-provider-wrapper.component";
import { TimeRangeProvider } from "./components/time-range/time-range-provider/time-range-provider.component";
import "./platform/assets/styles/fonts.scss";
import "./platform/assets/styles/layout.scss";
import {
    DialogProviderV1,
    NotificationProviderV1,
} from "./platform/components";
import { lightV1 } from "./platform/utils";
import { initLocale } from "./utils/locale/locale.util";

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
                    <AuthProviderWrapper>
                        <TimeRangeProvider>
                            <DialogProviderV1>
                                <App />
                            </DialogProviderV1>
                        </TimeRangeProvider>
                    </AuthProviderWrapper>
                </NotificationProviderV1>
            </BrowserRouter>
        </ThemeProvider>
    </StrictMode>,
    document.getElementById("root") as HTMLElement
);
