// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { PaletteColor } from "@material-ui/core/styles/createPalette";
import { ReactNode } from "react";

export interface LocalThemeProviderV1Props {
    primary?: PaletteColor;
    severity?: PaletteColor;
    children?: ReactNode;
}
