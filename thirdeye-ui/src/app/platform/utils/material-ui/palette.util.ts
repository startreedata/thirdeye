import { alpha } from "@material-ui/core";
import { PaletteOptions } from "@material-ui/core/styles/createPalette";
import { ColorV1 } from "./color.util";

// Material UI theme colors
export const PaletteV1 = {
    PrimaryColorMain: ColorV1.Blue2,
    SecondaryColorMain: ColorV1.Grey2,
    ActionColorLight: ColorV1.White1,
    HoverOpacityLight: 0.06,
    HoverOverlayLight: alpha(ColorV1.White1, 0.06),
    FocusOpacityLight: 0.09,
    FocusOverlayLight: alpha(ColorV1.White1, 0.09),
    SelectedOpacityLight: 0.12,
    SelectedOverlayLight: alpha(ColorV1.White1, 0.12),
    ActiveOpacityLight: 0.24,
    ActiveOverlayLight: alpha(ColorV1.White1, 0.24),
    ActionColorDark: ColorV1.Black1,
    HoverOpacityDark: 0.02,
    HoverOverlayDark: alpha(ColorV1.Black1, 0.02),
    FocusOpacityDark: 0.04,
    FocusOverlayDark: alpha(ColorV1.Black1, 0.04),
    SelectedOpacityDark: 0.06,
    SelectedOverlayDark: alpha(ColorV1.Black1, 0.06),
    ActiveOpacityDark: 0.12,
    ActiveOverlayDark: alpha(ColorV1.Black1, 0.12),
    NavBarBackgroundColor: ColorV1.Blue7,
    TooltipBackgroundColor: ColorV1.Black1,
    DataGridHeaderBackgroundColor: ColorV1.Grey6,
    PaperBackgroundColor: ColorV1.White1,
    BackgroundColorDefault: ColorV1.Grey4,
    BorderColorDefault: ColorV1.Grey5,
} as const;

// Material UI theme color palette
export const paletteOptionsV1: PaletteOptions = {
    primary: {
        light: ColorV1.Blue1,
        main: PaletteV1.PrimaryColorMain,
        dark: ColorV1.Blue3,
        contrastText: ColorV1.White1,
    },
    secondary: {
        light: ColorV1.Grey1,
        main: PaletteV1.SecondaryColorMain,
        dark: ColorV1.Grey3,
        contrastText: ColorV1.White1,
    },
    error: {
        light: ColorV1.Red1,
        main: ColorV1.Red2,
        dark: ColorV1.Red3,
    },
    warning: {
        light: ColorV1.Yellow1,
        main: ColorV1.Yellow2,
        dark: ColorV1.Yellow3,
    },
    info: {
        light: ColorV1.Blue4,
        main: ColorV1.Blue5,
        dark: ColorV1.Blue6,
    },
    success: {
        light: ColorV1.Green1,
        main: ColorV1.Green2,
        dark: ColorV1.Green3,
    },
    background: {
        paper: PaletteV1.PaperBackgroundColor,
        default: PaletteV1.BackgroundColorDefault,
    },
    action: {
        hoverOpacity: PaletteV1.HoverOpacityDark,
        hover: PaletteV1.HoverOverlayDark,
        focusOpacity: PaletteV1.FocusOpacityDark,
        focus: PaletteV1.FocusOverlayDark,
        selectedOpacity: PaletteV1.SelectedOpacityDark,
        selected: PaletteV1.SelectedOverlayDark,
        activatedOpacity: PaletteV1.ActiveOpacityDark,
        active: PaletteV1.ActiveOverlayDark,
    },
};
