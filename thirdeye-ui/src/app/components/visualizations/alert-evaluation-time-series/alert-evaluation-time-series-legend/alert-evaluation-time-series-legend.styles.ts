import { makeStyles } from "@material-ui/core";

export const useAlertEvaluationTimeSeriesLegendStyles = makeStyles((theme) => ({
    legend: {
        display: "flex",
        justifyContent: "space-between",
        marginLeft: 10,
        marginRight: 10,
    },
    legendItem: {
        cursor: "pointer",
        paddingRight: 10,
        "&:last-of-type": {
            paddingRight: 0,
        },
    },
    legendItemLabel: {
        paddingLeft: 10,
    },
    legendItemLabelDisabled: {
        color: theme.palette.text.disabled,
    },
}));
