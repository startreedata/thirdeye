import { darken, lighten, makeStyles, Theme } from "@material-ui/core";
import { Dimension } from "../../utils/material-ui-util/dimension-util";

export const useSnackbarProviderStyles = makeStyles((theme: Theme) => ({
    container: {
        width: Dimension.WIDTH_SNACKBAR_DEFAULT,
    },
    success: {
        color: `${
            theme.palette.type === "light"
                ? darken(theme.palette.success.main, 0.6)
                : lighten(theme.palette.success.main, 0.6)
        } !important`,
        backgroundColor: `${
            theme.palette.type === "light"
                ? lighten(theme.palette.success.main, 0.9)
                : darken(theme.palette.success.main, 0.9)
        } !important`,
        ...theme.typography.body2,
    },
    error: {
        color: `${
            theme.palette.type === "light"
                ? darken(theme.palette.error.main, 0.6)
                : lighten(theme.palette.error.main, 0.6)
        } !important`,
        backgroundColor: `${
            theme.palette.type === "light"
                ? lighten(theme.palette.error.main, 0.9)
                : darken(theme.palette.error.main, 0.9)
        } !important`,
        ...theme.typography.body2,
    },
    warning: {
        color: `${
            theme.palette.type === "light"
                ? darken(theme.palette.warning.main, 0.6)
                : lighten(theme.palette.warning.main, 0.6)
        } !important`,
        backgroundColor: `${
            theme.palette.type === "light"
                ? lighten(theme.palette.warning.main, 0.9)
                : darken(theme.palette.warning.main, 0.9)
        } !important`,
        ...theme.typography.body2,
    },
    info: {
        color: `${
            theme.palette.type === "light"
                ? darken(theme.palette.info.main, 0.6)
                : lighten(theme.palette.info.main, 0.6)
        } !important`,
        backgroundColor: `${
            theme.palette.type === "light"
                ? lighten(theme.palette.info.main, 0.9)
                : darken(theme.palette.info.main, 0.9)
        } !important`,
        ...theme.typography.body2,
    },
}));
