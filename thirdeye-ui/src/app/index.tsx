/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { CssBaseline, ThemeProvider } from "@material-ui/core";
import {
    DialogProviderV1,
    lightV1,
    NotificationProviderV1,
} from "@startree-ui/platform-ui";
import "@startree-ui/platform-ui/assets/styles/fonts.scss";
import "@startree-ui/platform-ui/assets/styles/layout.scss";
import { enableAllPlugins } from "immer";
import React, { StrictMode } from "react";
import ReactDOM from "react-dom";
import { BrowserRouter } from "react-router-dom";
import { App } from "./app";
import { AuthProviderWrapper } from "./components/auth-provider-wrapper/auth-provider-wrapper.component";
import { TimeRangeProvider } from "./components/time-range/time-range-provider/time-range-provider.component";
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
