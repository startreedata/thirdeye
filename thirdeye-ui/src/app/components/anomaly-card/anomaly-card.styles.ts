import { createStyles, makeStyles, Theme } from "@material-ui/core";

export const useAnomalyCardStyles = makeStyles((theme: Theme) => {
    return createStyles({
        deviationError: {
            color: theme.palette.error.main,
        },
    });
});
