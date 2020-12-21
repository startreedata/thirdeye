import { createStyles, makeStyles, Theme } from "@material-ui/core";
import { Palette } from "../../utils/material-ui-util/palette-util";

export const useAlertEvaluationTimeSeriesInternalStyles = makeStyles(
    (theme: Theme) => {
        return createStyles({
            axisLabel: {
                ...theme.typography.overline,
            },
            legendContainer: {
                display: "flex",
                justifyContent: "space-evenly",
            },
            legendItem: {
                cursor: "pointer",
            },
            legendItemDisabled: {
                color: Palette.COLOR_TEXT_GREY,
            },
            legendItemText: {
                paddingLeft: "10px",
                paddingTop: "1px",
            },
        });
    }
);
