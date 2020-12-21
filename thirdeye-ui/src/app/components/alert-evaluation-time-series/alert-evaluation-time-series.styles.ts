import { createStyles, makeStyles, Theme } from "@material-ui/core";

export const useAlertEvaluationTimeSeriesInternalStyles = makeStyles(
    (theme: Theme) => {
        return createStyles({
            axisLabel: {
                ...theme.typography.overline,
            },
        });
    }
);
