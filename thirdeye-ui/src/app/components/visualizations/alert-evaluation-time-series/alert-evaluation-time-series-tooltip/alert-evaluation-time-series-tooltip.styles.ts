import { makeStyles } from "@material-ui/core";

export const useAlertEvaluationTimeSeriesTooltipStyles = makeStyles({
    header: {
        marginTop: "5px",
        marginBottom: "5px",
        "&:first-of-type": {
            marginTop: "0px",
        },
        "&:last-of-type": {
            marginBottom: "0px",
        },
    },
});
