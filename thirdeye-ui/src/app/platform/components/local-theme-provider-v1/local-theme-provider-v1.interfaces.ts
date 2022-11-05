import { PaletteColor } from "@material-ui/core/styles/createPalette";
import { ReactNode } from "react";

export interface LocalThemeProviderV1Props {
    primary?: PaletteColor;
    severity?: PaletteColor;
    children?: ReactNode;
}
