import { makeStyles, Theme } from "@material-ui/core";
import { Palette } from "../../utils/material-ui-util/palette-util";

export const useAppBarStyles = makeStyles((theme: Theme) => ({
    container: {
        backgroundColor: Palette.COLOR_BACKGROUND_APP_BAR,
        // App bar to be always above drawer
        zIndex: theme.zIndex.drawer + 1,
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
