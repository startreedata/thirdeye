import { createStyles, makeStyles, Theme } from "@material-ui/core";
import { Border } from "../../utils/material-ui/border.util";
import { Dimension } from "../../utils/material-ui/dimension-util";
import { Palette } from "../../utils/material-ui/palette-util";

export const useAlertCardStyles = makeStyles((theme: Theme) => {
    return createStyles({
        container: {
            border: Border.BORDER_DEFAULT,
        },
        activeContainer: {
            background: `linear-gradient(${Palette.COLOR_SUCCESS_LIGHT} 8px, ${theme.palette.background.default})`,
        },
        inactiveContainer: {
            background: `linear-gradient(${Palette.COLOR_DISABLED_LIGHT} 8px, ${theme.palette.background.default})`,
        },
        activeText: {
            color: theme.palette.success.main,
        },
        inactiveText: {
            color: Palette.COLOR_TEXT_GREY,
        },
        divider: {
            height: Dimension.WIDTH_BORDER_DEFAULT,
            width: "100%",
            backgroundColor: Palette.COLOR_BORDER_DEFAULT,
        },
        highlight: {
            color: Palette.COLOR_TEXT_DEFAULT,
            backgroundColor: theme.palette.primary.main,
        },
    });
});
