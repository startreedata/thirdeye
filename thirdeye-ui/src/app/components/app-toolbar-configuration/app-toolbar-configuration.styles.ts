import { createStyles, makeStyles, Theme } from "@material-ui/core";
import { Palette } from "../../utils/material-ui-util/palette-util";

export const useAppToolbarConfigurationStyles = makeStyles((theme: Theme) => {
    return createStyles({
        container: {
            backgroundColor: Palette.COLOR_BACKGROUND_APP_TOOLBAR,
            // Toolbar to be always above drawer
            zIndex: theme.zIndex.drawer + 1,
        },
        link: {
            marginRight: "16px",
        },
        selectedLink: {
            color: Palette.COLOR_TEXT_DEFAULT,
        },
    });
});
