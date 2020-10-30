import { createStyles, makeStyles, Theme } from "@material-ui/core";
import { Palette } from "../../utils/material-ui/palette.util";
import { Shadow } from "../../utils/material-ui/shadow.util";

export const applicationBarStyles = makeStyles((theme: Theme) => {
    return createStyles({
        applicationBar: {
            backgroundColor: Palette.COLOR_BACKGROUND_APP_BAR,
            boxShadow: Shadow.BOX_SHADOW_APP_BAR,
            // AppBar to be always above Drawer
            zIndex: theme.zIndex.drawer + 1,
        },
        logo: {
            alignItems: "center",
            display: "flex",
            marginRight: "8px",
        },
        link: {
            alignItems: "center",
            display: "flex",
        },
        linkLeftAlign: {
            marginLeft: "8px",
            marginRight: "8px",
        },
        linkRightAlign: {
            marginLeft: "auto",
            marginRight: "8px",
        },
    });
});
