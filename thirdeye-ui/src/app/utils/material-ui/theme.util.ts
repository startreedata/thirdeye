import { createMuiTheme, Theme } from "@material-ui/core";
import { buttonClasses } from "./button.util";
import { cssBaselineClasses } from "./css-baseline.util";
import { gridProps } from "./grid.util";
import { linkClasses, linkProps } from "./link.util";
import { listItemTextClasses } from "./list.util";
import { paletteOptions } from "./palette.util";
import { shapeOptions } from "./shape.util";
import { tooltipClasses } from "./tooltip.util";
import { typographyOptions } from "./typography.util";

// Material-UI theme
export const theme: Theme = createMuiTheme({
    palette: paletteOptions,
    props: {
        MuiLink: linkProps,
        MuiGrid: gridProps,
    },
    overrides: {
        MuiCssBaseline: cssBaselineClasses,
        MuiListItemText: listItemTextClasses,
        MuiLink: linkClasses,
        MuiButton: buttonClasses,
        MuiTooltip: tooltipClasses,
    },
    shape: shapeOptions,
    typography: typographyOptions,
});
