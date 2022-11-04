import { makeStyles } from "@material-ui/core";
import { codeTypographyOptionsV1 } from "../../../platform/utils";

export const useMetricCardStyles = makeStyles((theme) => ({
    active: {
        color: theme.palette.success.main,
    },
    inactive: {
        color: theme.palette.text.disabled,
    },
    query: {
        ...codeTypographyOptionsV1.body2,
    },
}));
