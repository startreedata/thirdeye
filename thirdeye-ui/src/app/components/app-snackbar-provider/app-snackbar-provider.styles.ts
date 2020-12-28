import { makeStyles, Theme } from "@material-ui/core";
import { Dimension } from "../../utils/material-ui-util/dimension-util";
import { Palette } from "../../utils/material-ui-util/palette-util";

export const useAppSnackbarProviderStyles = makeStyles((theme: Theme) => ({
    container: {
        width: Dimension.WIDTH_SNACKBAR_DEFAULT,
    },
    success: {
        color: `${Palette.COLOR_TEXT_DEFAULT} !important`,
        backgroundColor: `${Palette.COLOR_BACKGROUND_ALERT_SUCCESS} !important`,
        ...theme.typography.body1,
    },
    error: {
        color: `${Palette.COLOR_TEXT_DEFAULT} !important`,
        backgroundColor: `${Palette.COLOR_BACKGROUND_ALERT_ERROR} !important`,
        ...theme.typography.body1,
    },
    warning: {
        color: `${Palette.COLOR_TEXT_DEFAULT} !important`,
        backgroundColor: `${Palette.COLOR_BACKGROUND_ALERT_WARNING} !important`,
        ...theme.typography.body1,
    },
    info: {
        color: `${Palette.COLOR_TEXT_DEFAULT} !important`,
        backgroundColor: `${Palette.COLOR_BACKGROUND_ALERT_INFO} !important`,
        ...theme.typography.body1,
    },
}));
