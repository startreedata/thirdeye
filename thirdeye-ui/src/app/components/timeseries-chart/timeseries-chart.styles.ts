import { createStyles, makeStyles } from "@material-ui/core";
import { Palette } from "../../utils/material-ui/palette-util";

export const useTimeseriesChartStyles = makeStyles(() => {
    return createStyles({
        tooltipWithBounds: {
            background: Palette.COLOR_BACKGROUND_DEFAULT,
            border: "1px solid " + Palette.COLOR_TEXT_DEFAULT,
            color: Palette.COLOR_TEXT_DEFAULT,
        },
        tooltip: {
            minWidth: 72,
            textAlign: "center",
            transform: "translateX(-50%)",
        },
    });
});

export const selectedBrushStyle = {
    fill: `url(#brush_pattern)`,
    stroke: "black",
};
