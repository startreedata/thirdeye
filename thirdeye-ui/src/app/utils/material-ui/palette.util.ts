import { PaletteOptions } from "@material-ui/core/styles/createPalette";
import { Color } from "./color.util";

// Constants for convenience, to be shared across Palette and PaletteOptions
const COLOR_PRIMARY_CONTRAST_TEXT = Color.WHITE_1;
const COLOR_PRIMARY_DARK = Color.TEAL_2;
const COLOR_PRIMARY_LIGHT = Color.TEAL_3;
const COLOR_PRIMARY_MAIN = Color.TEAL_1;
const COLOR_SECONDARY_CONTRAST_TEXT = Color.WHITE_1;
const COLOR_SECONDARY_DARK = Color.GREEN_2;
const COLOR_SECONDARY_LIGHT = Color.GREEN_3;
const COLOR_SECONDARY_MAIN = Color.GREEN_1;
const COLOR_BACKGROUND_DEFAULT = Color.WHITE_1;
const COLOR_BACKGROUND_PAPER = Color.WHITE_1;

// Material-UI theme colors
export const Palette = {
    COLOR_BORDER_DEFAULT: Color.GREY_1,
    COLOR_BORDER_DARK: Color.BLACK_1,
    COLOR_BORDER_LIGHT: Color.GREY_2,
    COLOR_BACKGROUND_TOOLTIP: Color.GREY_3,
    // Visualizations
    COLOR_VISUALIZATION_STROKE_CURRENT: COLOR_PRIMARY_DARK,
    COLOR_VISUALIZATION_STROKE_BASELINE: COLOR_PRIMARY_DARK,
    COLOR_VISUALIZATION_FILL_UPPER_AND_LOWER_BOUND: COLOR_PRIMARY_MAIN,
    COLOR_VISUALIZATION_FILL_ANOMALY: Color.RED_1,
    COLOR_VISUALIZATION_STROKE_BRUSH: Color.GREY_1,
    COLOR_VISUALIZATION_STROKE_HOVER_MARKER: Color.GREY_3,
    COLOR_VISUALIZATION_FILL_HOVER_MARKER: Color.WHITE_1,
} as const;

// Material-UI theme color palette
export const paletteOptions: PaletteOptions = {
    primary: {
        contrastText: COLOR_PRIMARY_CONTRAST_TEXT,
        dark: COLOR_PRIMARY_DARK,
        light: COLOR_PRIMARY_LIGHT,
        main: COLOR_PRIMARY_MAIN,
    },
    secondary: {
        contrastText: COLOR_SECONDARY_CONTRAST_TEXT,
        dark: COLOR_SECONDARY_DARK,
        light: COLOR_SECONDARY_LIGHT,
        main: COLOR_SECONDARY_MAIN,
    },
    background: {
        default: COLOR_BACKGROUND_DEFAULT,
        paper: COLOR_BACKGROUND_PAPER,
    },
};
