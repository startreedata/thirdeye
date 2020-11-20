import { PaletteOptions } from "@material-ui/core/styles/createPalette";
import { Color } from "./color-util";

// CortexData Material-UI theme component colors
export const Palette = {
    COLOR_BACKGROUND_APP_BAR: Color.WHITE,
    COLOR_BACKGROUND_BREADCRUMBS: Color.WHITE,
    COLOR_BORDER_DEFAULT: Color.GREY,
} as const;

// CortexData Material-UI theme color palette
export const paletteOptions = <PaletteOptions>{
    primary: {
        contrastText: Color.WHITE,
        dark: Color.TEAL_DARK,
        light: Color.TEAL_LIGHT,
        main: Color.TEAL,
    },
    secondary: {
        contrastText: Color.WHITE,
        dark: Color.GREEN_DARK,
        light: Color.GREEN_LIGHT,
        main: Color.GREEN,
    },
    background: {
        paper: Color.WHITE,
        default: Color.WHITE,
    },
};
