import { makeStyles } from "@material-ui/core";

export const useAlertEvaluationTimeSeriesTooltipStyles = makeStyles({
    header: {
        marginTop: 5,
        marginBottom: 5,
        "&:first-of-type": {
            marginTop: 0,
        },
        "&:last-of-type": {
            marginBottom: 0,
        },
    },
});
