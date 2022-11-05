import { makeStyles } from "@material-ui/core";

export const useTooltipV1Styles = makeStyles((theme) => ({
    tooltip: {
        ...theme.typography.subtitle2,
        borderRadius: theme.shape.borderRadius,
    },
}));
