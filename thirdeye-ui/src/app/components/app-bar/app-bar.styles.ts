import { makeStyles, Theme } from "@material-ui/core";
import { Palette } from "../../utils/material-ui-util/palette-util";

export const useAppBarStyles = makeStyles((theme: Theme) => ({
    container: {
        backgroundColor: Palette.COLOR_BACKGROUND_APP_BAR,
        boxShadow:
            "0px 8px 8px rgba(0, 0, 0, 0.04), 0px 16px 22px rgba(0, 0, 0, 0.04), 0px 8px 26px rgba(41, 148, 169, 0.1), 0px 0px 2px rgba(110, 196, 209, 0.12)",
        // App bar to be always above drawer and breadcrumbs
        zIndex: theme.zIndex.drawer + 2,
    },
    link: {
        display: "flex",
        marginRight: "16px",
        "&:last-of-type": {
            marginRight: "0px",
        },
    },
    rightAlign: {
        marginLeft: "auto",
        marginRight: "16px",
    },
    selectedLink: {
        color: Palette.COLOR_TEXT_DEFAULT,
    },
}));
