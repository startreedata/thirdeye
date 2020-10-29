import { PaletteOptions } from "@material-ui/core/styles/createPalette";
import { Color } from "./color.util";

// CortexData Material-UI theme component colors
export const Palette = {
    COLOR_APP_BAR: Color.BLACK,
    COLOR_BACKGROUND_APP_BAR: Color.WHITE,
    COLOR_PRIMARY: Color.TEAL, // same as paletteOptions.primary.main for convenience
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
};
