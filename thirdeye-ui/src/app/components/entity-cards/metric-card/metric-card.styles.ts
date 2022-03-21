import { makeStyles } from "@material-ui/core";
import { codeTypographyOptions } from "../../../utils/material-ui/typography.util";

export const useMetricCardStyles = makeStyles((theme) => ({
    active: {
        color: theme.palette.success.main,
    },
    inactive: {
        color: theme.palette.text.disabled,
    },
    query: {
        ...codeTypographyOptions.body2,
    },
}));
