import { makeStyles } from "@material-ui/core";

export const useAlertEvaluationTimeSeriesCardStyles = makeStyles((theme) => ({
    helperText: {
        paddingRight: theme.spacing(1),
    },
    refreshIcon: {
        color: theme.palette.text.primary,
    },
}));
