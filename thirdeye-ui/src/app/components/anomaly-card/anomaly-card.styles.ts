import { createStyles, makeStyles, Theme } from "@material-ui/core";
import { Border } from "../../utils/material-ui/border.util";
import { Dimension } from "../../utils/material-ui/dimension-util";
import { Palette } from "../../utils/material-ui/palette-util";

export const useAnomalyCardStyles = makeStyles((theme: Theme) => {
    return createStyles({
        container: {
            border: Border.BORDER_DEFAULT,
        },
        header: {
            background: `linear-gradient(${Palette.COLOR_ERROR_LIGHT} 8px, ${theme.palette.background.default})`,
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
