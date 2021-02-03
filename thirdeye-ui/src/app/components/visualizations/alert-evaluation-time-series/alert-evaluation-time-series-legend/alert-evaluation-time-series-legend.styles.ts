import { makeStyles, Theme } from "@material-ui/core";

export const useAlertEvaluationTimeSeriesLegendStyles = makeStyles(
    (theme: Theme) => ({
        legendContainer: {
            display: "flex",
            justifyContent: "space-between",
            marginLeft: "10px",
            marginRight: "10px",
        },
        legendItem: {
            cursor: "pointer",
        },
        legendItemDisabled: {
            color: theme.palette.text.disabled,
        },
        legendItemText: {
            paddingLeft: "10px",
            paddingTop: "1px",
        },
    })
);
