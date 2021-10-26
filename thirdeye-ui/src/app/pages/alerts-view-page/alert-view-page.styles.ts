import { makeStyles } from "@material-ui/core";
import { Palette } from "../../utils/material-ui/palette.util";

export const useAlertDetailsStyles = makeStyles((theme) => ({
    background: {
        minWidth: 0,
        padding: theme.spacing(0.625),
        borderWidth: theme.spacing(0.125),
        borderStyle: "solid",
        backgroundColor: theme.palette.secondary.contrastText,
        border: theme.spacing(0.125),
        borderColor: Palette.COLOR_BORDER_DEFAULT,
        height: theme.spacing(4.5),
    },

    active: {
        display: "flex",
        flexDirection: "row",
        borderStyle: "solid",
        borderWidth: theme.spacing(0.125),
        backgroundColor: theme.palette.secondary.contrastText,
        borderColor: Palette.COLOR_BORDER_DEFAULT,
        padding: theme.spacing(0.5, 2.5, 0.5, 0.5),
        gap: theme.spacing(1),
        alignItems: "center",
        borderRadius: theme.spacing(0.5),
        textTransform: "capitalize",
        height: theme.spacing(4.5),
        "& svg": {
            display: "flex",
            justifyContent: "flex-end",
            fontSize: theme.spacing(3.125),
        },
    },
    smallSize: {
        fontSize: theme.spacing(1.5),
    },
}));
