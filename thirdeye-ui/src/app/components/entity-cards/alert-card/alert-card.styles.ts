import { makeStyles, Theme } from "@material-ui/core";

export const useAlertCardStyles = makeStyles((theme: Theme) => ({
    activeText: {
        color: theme.palette.success.main,
    },
    inactiveText: {
        color: theme.palette.text.disabled,
    },
}));
