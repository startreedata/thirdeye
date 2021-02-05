import { makeStyles, Theme } from "@material-ui/core";

export const useTimeAxisBottomStyles = makeStyles((theme: Theme) => ({
    tick: {
        ...theme.typography.overline,
    },
}));
