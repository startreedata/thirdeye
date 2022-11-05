import { makeStyles } from "@material-ui/core";
import { darken, lighten } from "@material-ui/core/styles/colorManipulator";

export const useAlertWizardV2Styles = makeStyles((theme) => ({
    label: {
        color: theme.palette.text.primary,
    },
    autoCompleteInput: {
        paddingRight: "45px",
    },
    infoAlert: {
        backgroundColor: lighten(theme.palette.primary.light, 0.9),
        color: darken(theme.palette.primary.light, 0.5),
    },
    warningAlert: {
        backgroundColor: lighten(theme.palette.warning.light, 0.9),
        color: darken(theme.palette.warning.light, 0.5),
    },
}));
