import { createStyles, makeStyles, Theme } from "@material-ui/core";
import { Color } from "../../utils/material-ui/color-util";
import { Palette } from "../../utils/material-ui/palette-util";

export const useApplicationBreadcrumbsStyles = makeStyles((theme: Theme) => {
    return createStyles({
        container: {
            backgroundColor: Palette.COLOR_BACKGROUND_BREADCRUMBS,
            // Breadcrumbs to be always above Drawer
            zIndex: theme.zIndex.drawer + 1,
        },
        selected: {
            color: Color.BLACK,
            cursor: "default",
        },
    });
});
