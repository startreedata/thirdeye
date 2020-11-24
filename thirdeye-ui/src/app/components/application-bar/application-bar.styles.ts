import { createStyles, makeStyles, Theme } from "@material-ui/core";
import { Palette } from "../../utils/material-ui/palette-util";

export const useApplicationBarStyles = makeStyles((theme: Theme) => {
    return createStyles({
        appBar: {
            backgroundColor: Palette.COLOR_BACKGROUND_APP_BAR,
            boxShadow:
                "0px 8px 8px rgba(0, 0, 0, 0.04), 0px 16px 22px rgba(0, 0, 0, 0.04), 0px 8px 26px rgba(41, 148, 169, 0.1), 0px 0px 2px rgba(110, 196, 209, 0.12)",
            // AppBar to be always above Drawer and Breadcrumbs
            zIndex: theme.zIndex.drawer + 2,
        },
        logo: {
            alignItems: "center",
            display: "flex",
            marginRight: "8px",
        },
        link: {
            alignItems: "center",
            display: "flex",
            marginLeft: "8px",
            marginRight: "8px",
            "&:last-of-type": {
                marginRight: "0px",
            },
        },
        selected: {
            color: Palette.COLOR_TEXT_DEFAULT,
        },
        rightAlign: {
            marginLeft: "auto",
            marginRight: "8px",
        },
    });
});
