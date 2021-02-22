import { makeStyles } from "@material-ui/core";

export const useCommonStyles = makeStyles((theme) => ({
    gridLimitation: {
        // Padding to avoid the effect of Material-UI Grid negative margins
        // (https://material-ui.com/components/grid/#limitations)
        padding: theme.spacing(1),
    },
    cardContentBottomPaddingRemoved: {
        "&:last-child": {
            paddingBottom: 0,
        },
    },
    ellipsis: {
        whiteSpace: "nowrap",
        overflow: "hidden",
        textOverflow: "ellipsis",
    },
    backdrop: {
        backgroundColor: "rgba(0, 0, 0, 0.5)",
    },
}));
