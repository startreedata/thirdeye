import { createMuiTheme, Theme } from "@material-ui/core";
import { buttonClasses } from "./button-util";
import { gridProps } from "./grid-util";
import { linkProps } from "./link-util";
import { paletteOptions } from "./palette-util";
import { shapeOptions } from "./shape-util";
import { typographyOptions } from "./typography-util";

// CortexData Material-UI theme
export const theme: Theme = createMuiTheme({
    palette: paletteOptions,
    props: {
        MuiGrid: gridProps,
        MuiLink: linkProps,
    },
    overrides: {
        MuiButton: buttonClasses,
    },
    shape: shapeOptions,
    typography: typographyOptions,
});
