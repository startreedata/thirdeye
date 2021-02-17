import { makeStyles } from "@material-ui/core";

export const useAlertEvaluationTimeSeriesLegendStyles = makeStyles((theme) => ({
    legend: {
        display: "flex",
        justifyContent: "space-between",
        marginLeft: "10px",
        marginRight: "10px",
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
}));
