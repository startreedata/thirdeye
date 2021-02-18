import { makeStyles } from "@material-ui/core";

export const useAlertCardStyles = makeStyles((theme) => ({
    active: {
        color: theme.palette.success.main,
    },
    inactive: {
        color: theme.palette.text.disabled,
    },
}));
