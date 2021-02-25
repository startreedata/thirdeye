import { darken, lighten, makeStyles } from "@material-ui/core";
import { Dimension } from "../../utils/material-ui/dimension.util";

export const useSnackbarProviderStyles = makeStyles((theme) => ({
    snackbarProvider: {
        // Default 100% width when screen width is xs
        [theme.breakpoints.up("sm")]: {
            width: Dimension.WIDTH_SNACKBAR_DEFAULT,
        },
    },
    success: {
        ...theme.typography.body2,
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
    },
    error: {
        ...theme.typography.body2,
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
    },
    warning: {
        ...theme.typography.body2,
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
    },
    info: {
        ...theme.typography.body2,
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
    },
}));
