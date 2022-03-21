import { PaletteOptions } from "@material-ui/core/styles/createPalette";
import { Color } from "./color.util";

// Constants for convenience, to be shared across Palette and paletteOptions
const COLOR_PRIMARY_LIGHT = Color.TEAL_1;
const COLOR_PRIMARY_MAIN = Color.TEAL_2;
const COLOR_PRIMARY_DARK = Color.TEAL_3;
const COLOR_PRIMARY_CONTRAST_TEXT = Color.WHITE_1;
const COLOR_SECONDARY_LIGHT = Color.GREEN_1;
const COLOR_SECONDARY_MAIN = Color.GREEN_2;
const COLOR_SECONDARY_DARK = Color.GREEN_3;
const COLOR_SECONDARY_CONTRAST_TEXT = Color.WHITE_1;
const COLOR_SUCCESS_LIGHT = Color.GREEN_4;
const COLOR_SUCCESS_MAIN = Color.GREEN_5;
const COLOR_SUCCESS_DARK = Color.GREEN_6;
const COLOR_SUCCESS_CONTRAST_TEXT = Color.GREY_1;
const COLOR_ERROR_LIGHT = Color.RED_1;
const COLOR_ERROR_MAIN = Color.RED_2;
const COLOR_ERROR_DARK = Color.RED_3;
const COLOR_ERROR_CONTRAST_TEXT = Color.WHITE_1;
const COLOR_BACKGROUND_PAPER = Color.WHITE_1;
const COLOR_BACKGROUND_DEFAULT = Color.WHITE_1;

// Material-UI theme colors
export const Palette = {
    COLOR_BORDER_DEFAULT: Color.GREY_2,
    COLOR_BORDER_INPUT_DEFAULT: Color.GREY_3,
    COLOR_BORDER_INPUT_ERROR: COLOR_ERROR_MAIN,
    COLOR_FONT_TOOLTIP: Color.BLACK_1,
    COLOR_BACKGROUND_TOOLTIP: Color.GREY_4,
    COLOR_BACKGROUND_BACKDROP: Color.BLACK_2,
    // Visualizations
    COLOR_VISUALIZATION_STROKE_CURRENT: Color.GREY_4,
    COLOR_VISUALIZATION_STROKE_BASELINE: COLOR_PRIMARY_DARK,
    COLOR_VISUALIZATION_STROKE_UPPER_AND_LOWER_BOUND: COLOR_PRIMARY_LIGHT,
    COLOR_VISUALIZATION_STROKE_ANOMALY: COLOR_ERROR_MAIN,
    COLOR_VISUALIZATION_STROKE_BRUSH: Color.GREY_3,
    COLOR_VISUALIZATION_STROKE_HOVER_MARKER: Color.GREY_4,
    COLOR_VISUALIZATION_FILL_HOVER_MARKER: Color.WHITE_1,
} as const;

// Material-UI theme color palette
export const paletteOptions: PaletteOptions = {
    primary: {
        light: COLOR_PRIMARY_LIGHT,
        main: COLOR_PRIMARY_MAIN,
        dark: COLOR_PRIMARY_DARK,
        contrastText: COLOR_PRIMARY_CONTRAST_TEXT,
    },
    secondary: {
        light: COLOR_SECONDARY_LIGHT,
        main: COLOR_SECONDARY_MAIN,
        dark: COLOR_SECONDARY_DARK,
        contrastText: COLOR_SECONDARY_CONTRAST_TEXT,
    },
    success: {
        light: COLOR_SUCCESS_LIGHT,
        main: COLOR_SUCCESS_MAIN,
        dark: COLOR_SUCCESS_DARK,
        contrastText: COLOR_SUCCESS_CONTRAST_TEXT,
    },
    error: {
        light: COLOR_ERROR_LIGHT,
        main: COLOR_ERROR_MAIN,
        dark: COLOR_ERROR_DARK,
        contrastText: COLOR_ERROR_CONTRAST_TEXT,
    },
    background: {
        paper: COLOR_BACKGROUND_PAPER,
        default: COLOR_BACKGROUND_DEFAULT,
    },
};
