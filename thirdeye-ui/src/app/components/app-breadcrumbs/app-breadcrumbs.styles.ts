import { createStyles, makeStyles, Theme } from "@material-ui/core";
import { Palette } from "../../utils/material-ui-util/palette-util";

export const useAppBreadcrumbsStyles = makeStyles((theme: Theme) => {
    return createStyles({
        container: {
            backgroundColor: Palette.COLOR_BACKGROUND_BREADCRUMBS,
            // Breadcrumbs to be always above Drawer
            zIndex: theme.zIndex.drawer + 1,
        },
        dense: {
            minHeight: "36px",
        },
        link: {
            alignItems: "center",
            display: "flex",
        },
        selected: {
            color: Palette.COLOR_TEXT_DEFAULT,
            cursor: "default",
        },
    });
});
