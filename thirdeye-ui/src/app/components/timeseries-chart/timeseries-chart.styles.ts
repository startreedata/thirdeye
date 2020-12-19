import { createStyles, makeStyles } from "@material-ui/core";
import { Palette } from "../../utils/material-ui-util/palette-util";

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
        },
        grayout: {
            opacity: 0.5,
        },
        clickable: {
            cursor: "pointer",
        },
        legends: {
            position: "absolute",
            bottom: 0,
            left: 0,
            right: 0,
            display: "flex",
            justifyContent: "space-around",
        },
        legendsShape: {
            display: "flex",
            margin: "2px 4px 2px 0",
        },
        noDataLabel: {
            position: "absolute",
            left: 0,
            right: 0,
            top: "50%",
            textAlign: "center",
            transform: "translateY(-50%)",
        },
    });
});

export const selectedBrushStyle = {
    fill: `url(#brush_pattern)`,
    stroke: "black",
};
