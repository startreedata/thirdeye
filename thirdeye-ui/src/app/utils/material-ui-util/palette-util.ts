import { PaletteOptions } from "@material-ui/core/styles/createPalette";
import { Color } from "./color-util";

// Material-UI theme component colors
export const Palette = {
    COLOR_BORDER_DEFAULT: Color.GREY_1,
    COLOR_BORDER_DARK: Color.BLACK_1,
    // Visualizations
    COLOR_VISUALIZATION_STROKE_DEFAULT: Color.BLACK_1,
    COLOR_VISUALIZATION_STROKE_GREY: Color.GREY_1,
    COLOR_VISUALIZATION_STROKE_BASELINE: Color.ORANGE_1,
} as const;

// Material-UI theme color palette
export const paletteOptions: PaletteOptions = {
    primary: {
        contrastText: Color.WHITE_1,
        dark: Color.TEAL_2,
        light: Color.TEAL_3,
        main: Color.TEAL_1,
    },
    secondary: {
        contrastText: Color.WHITE_1,
        dark: Color.GREEN_2,
        light: Color.GREEN_3,
        main: Color.GREEN_1,
    },
    background: {
        paper: Color.WHITE_1,
        default: Color.WHITE_1,
    },
};
