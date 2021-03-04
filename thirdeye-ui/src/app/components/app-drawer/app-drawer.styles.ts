import { makeStyles } from "@material-ui/core";
import { Dimension } from "../../utils/material-ui/dimension.util";

export const useAppDrawerStyles = makeStyles((theme) => ({
    drawerPaper: {
        width: Dimension.WIDTH_DRAWER_DEFAULT,
        transition: theme.transitions.create(["width"], {
            duration: theme.transitions.duration.enteringScreen,
            easing: theme.transitions.easing.sharp,
        }),
    },
    drawerPaperMinimized: {
        width: Dimension.WIDTH_DRAWER_MINIMIZED,
        transition: theme.transitions.create(["width"], {
            duration: theme.transitions.duration.leavingScreen,
            easing: theme.transitions.easing.sharp,
        }),
    },
    appDrawerContents: {
        overflowY: "auto",
    },
}));
