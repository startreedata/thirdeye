import { createStyles, makeStyles, Theme } from "@material-ui/core";
import { Palette } from "../../utils/material-ui-util/palette-util";

export const useConfigurationToolbarStyles = makeStyles((theme: Theme) => {
    return createStyles({
        container: {
            backgroundColor: Palette.COLOR_BACKGROUND_TOOLBAR,
            // Toolbar to be always above Drawer
            zIndex: theme.zIndex.drawer + 1,
        },
        link: {
            alignItems: "center",
            display: "flex",
            marginLeft: "8px",
            marginRight: "8px",
        },
        selected: {
            color: Palette.COLOR_TEXT_DEFAULT,
        },
    });
});
