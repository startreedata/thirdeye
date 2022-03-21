import { makeStyles } from "@material-ui/core";

export const useActiveIndicatorStyles = makeStyles((theme) => ({
    indicator: {
        paddingLeft: theme.spacing(1),
        paddingRight: theme.spacing(1),
    },
}));
