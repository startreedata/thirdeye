import { makeStyles } from "@material-ui/core";
import { darken, lighten } from "@material-ui/core/styles/colorManipulator";

export const useRCACHartLegendStyles = makeStyles((theme) => ({
    infoAlert: {
        backgroundColor: lighten(theme.palette.primary.light, 0.9),
        color: darken(theme.palette.primary.light, 0.5),
    },
}));
