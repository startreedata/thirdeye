import { makeStyles } from "@material-ui/core";

export const useTimeAxisBottomStyles = makeStyles((theme) => ({
    tick: {
        ...theme.typography.overline,
    },
}));
