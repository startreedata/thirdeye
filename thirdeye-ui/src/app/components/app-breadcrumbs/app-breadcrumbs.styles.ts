import { makeStyles, Theme } from "@material-ui/core";
import { Palette } from "../../utils/material-ui-util/palette-util";

export const useAppBreadcrumbsStyles = makeStyles((theme: Theme) => ({
    container: {
        backgroundColor: Palette.COLOR_BACKGROUND_APP_BREADCRUMBS,
        // Breadcrumbs to be always above drawer
        zIndex: theme.zIndex.drawer + 1,
    },
    dense: {
        minHeight: "36px",
    },
    selectedLink: {
        color: Palette.COLOR_TEXT_DEFAULT,
        cursor: "default",
    },
}));
