import { createStyles, makeStyles, Theme } from "@material-ui/core";
import { Color } from "../../utils/material-ui/color.util";
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
            marginLeft: "8px",
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
            color: Color.BLACK,
        },
        rightAlign: {
            marginLeft: "auto",
            marginRight: "8px",
        },
    });
});
