import { createStyles, makeStyles, Theme } from "@material-ui/core";
import { Palette } from "../../utils/material-ui/palette-util";

export const useAlertCardStyles = makeStyles((theme: Theme) => {
    return createStyles({
        activeText: {
            color: theme.palette.success.main,
        },
        inactiveText: {
            color: Palette.COLOR_TEXT_GREY,
        },
        bottomRowLabel: {
            float: "left",
        },
        bottomRowIcon: {
            marginBottom: "-6px",
        },
        bottomRowValue: {
            clear: "both",
        },
    });
});
