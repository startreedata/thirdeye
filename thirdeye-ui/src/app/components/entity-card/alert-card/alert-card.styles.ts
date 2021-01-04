import { makeStyles, Theme } from "@material-ui/core";
import { Palette } from "../../../utils/material-ui-util/palette-util";

export const useAlertCardStyles = makeStyles((theme: Theme) => ({
    activeText: {
        color: theme.palette.success.main,
    },
    inactiveText: {
        color: Palette.COLOR_TEXT_GREY,
    },
}));
