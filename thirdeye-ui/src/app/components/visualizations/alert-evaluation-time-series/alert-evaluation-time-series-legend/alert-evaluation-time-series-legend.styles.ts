import { makeStyles, Theme } from "@material-ui/core";

export const useAlertEvaluationTimeSeriesLegendStyles = makeStyles(
    (theme: Theme) => ({
        container: {
            display: "flex",
            justifyContent: "space-between",
        },
        legendItem: {
            cursor: "pointer",
        },
        legendItemLabel: {
            paddingLeft: "10px",
        },
        legendItemLabelDisabled: {
            color: theme.palette.text.disabled,
        },
    })
);
