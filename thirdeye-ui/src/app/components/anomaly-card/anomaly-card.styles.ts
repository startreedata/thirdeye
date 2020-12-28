import { makeStyles, Theme } from "@material-ui/core";

export const useAnomalyCardStyles = makeStyles((theme: Theme) => ({
    deviation: {
        color: theme.palette.error.main,
    },
}));
