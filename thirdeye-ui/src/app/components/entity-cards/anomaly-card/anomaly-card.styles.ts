import { makeStyles } from "@material-ui/core";

export const useAnomalyCardStyles = makeStyles((theme) => ({
    deviation: {
        color: theme.palette.error.main,
    },
}));
