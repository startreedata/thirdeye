import { makeStyles } from "@material-ui/core";

export const useAlertEvaluationTimeSeriesLegendStyles = makeStyles((theme) => ({
    legendItem: {
        cursor: "pointer",
        paddingLeft: theme.spacing(1),
        paddingRight: theme.spacing(1),
    },
    legendItemWrapped: {
        marginRight: `${theme.spacing(18)}px !important`,
    },
    legendLabel: {
        paddingLeft: theme.spacing(1),
    },
    legendLabelDisabled: {
        color: theme.palette.text.disabled,
    },
}));
