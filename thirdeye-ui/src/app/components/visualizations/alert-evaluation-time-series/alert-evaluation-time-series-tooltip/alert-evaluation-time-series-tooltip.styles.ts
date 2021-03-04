import { makeStyles } from "@material-ui/core";

export const useAlertEvaluationTimeSeriesTooltipStyles = makeStyles(
    (theme) => ({
        alertEvaluationTimeSeriesTooltip: {
            minWidth: 140,
        },
        time: {
            marginBottom: theme.spacing(1),
        },
        nameValueContents: {
            width: "100%",
            display: "table",
        },
        name: {
            display: "table-cell",
            verticalAlign: "middle",
        },
        value: {
            display: "table-cell",
            textAlign: "right",
            verticalAlign: "middle",
            paddingLeft: theme.spacing(1),
        },
        anomaly: {
            marginTop: theme.spacing(1),
            marginBottom: theme.spacing(1),
        },
        more: {
            marginTop: theme.spacing(1),
        },
    })
);
