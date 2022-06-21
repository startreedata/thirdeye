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
import { ThemeProvider, useTheme } from "@material-ui/core";
import { cloneDeep } from "lodash";
import React, { FunctionComponent } from "react";
import { LocalThemeProviderV1Props } from "./local-theme-provider-v1.interfaces";

export const LocalThemeProviderV1: FunctionComponent<
    LocalThemeProviderV1Props
> = ({ primary, children }) => {
    const theme = useTheme();
    const localTheme = cloneDeep(theme);

    // Customize theme
    if (primary) {
        localTheme.palette.primary = primary;
    }

    return <ThemeProvider theme={localTheme}>{children}</ThemeProvider>;
};
