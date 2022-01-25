import { makeStyles } from "@material-ui/core";
import { Palette } from "../../../utils/material-ui/palette.util";

export const useTooltipWithBoundsStyles = makeStyles(() => ({
    tooltipContainer: {
        position: "relative",
    },
    tooltip: {
        color: `${Palette.COLOR_FONT_TOOLTIP} !important`,
        zIndex: 999,
        filter: "drop-shadow(rgba(0, 0, 0, 0.5) 0 2px 10px)",
    },
}));
