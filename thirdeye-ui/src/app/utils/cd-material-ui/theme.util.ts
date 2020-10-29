import { createMuiTheme, Theme } from "@material-ui/core";
import { gridProps } from "./grid.util";
import { linkProps } from "./link.util";
import { paletteOptions } from "./palette.util";
import { typographyOptions } from "./typography.util";

// CortexData Material-UI theme
export const theme = <Theme>createMuiTheme({
    palette: paletteOptions,
    props: {
        MuiGrid: gridProps,
        MuiLink: linkProps,
    },
    typography: typographyOptions,
});
