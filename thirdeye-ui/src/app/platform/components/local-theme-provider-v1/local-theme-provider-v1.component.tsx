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
