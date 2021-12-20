import { makeStyles } from "@material-ui/core";
import { Palette } from "../../../utils/material-ui/palette.util";

export const useTooltipWithBoundsStyles = makeStyles((theme) => ({
    tooltipContainer: {
        position: "relative",
    },
    tooltip: {
        color: `${theme.palette.primary.contrastText} !important`,
        backgroundColor: `${Palette.COLOR_BACKGROUND_TOOLTIP} !important`,
        zIndex: 999,
    },
}));
