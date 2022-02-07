import { createTheme, Theme } from "@material-ui/core";
import { buttonClasses } from "./button.util";
import { cssBaselineClasses } from "./css-baseline.util";
import { gridProps } from "./grid.util";
import { linkClasses, linkProps } from "./link.util";
import { listItemTextClasses } from "./list.util";
import { menuClasses, menuProps } from "./menu.util";
import { paletteOptions } from "./palette.util";
import { popoverProps } from "./popover.util";
import { shapeOptions } from "./shape.util";
import { typographyOptions } from "./typography.util";

// Material-UI theme
export const theme: Theme = createTheme({
    palette: paletteOptions,
    props: {
        MuiLink: linkProps,
        MuiMenu: menuProps,
        MuiGrid: gridProps,
        MuiPopover: popoverProps,
    },
    overrides: {
        MuiCssBaseline: cssBaselineClasses,
        MuiLink: linkClasses,
        MuiMenu: menuClasses,
        MuiListItemText: listItemTextClasses,
        MuiButton: buttonClasses,
    },
    shape: shapeOptions,
    typography: typographyOptions,
});
