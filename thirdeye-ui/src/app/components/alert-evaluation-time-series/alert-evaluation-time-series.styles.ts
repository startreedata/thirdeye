import { makeStyles, Theme } from "@material-ui/core";

export const useAlertEvaluationTimeSeriesInternalStyles = makeStyles(
    (theme: Theme) => ({
        axisLabel: {
            ...theme.typography.overline,
        },
    })
);
