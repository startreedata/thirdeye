import { makeStyles } from "@material-ui/core";

export const useDimensionHeatmapTooltipStyles = makeStyles((theme) => ({
    spaceBottom: {
        marginBottom: theme.spacing(1),
    },
    dataDisplayList: {
        minWidth: "300px;",
    },
    decreased: {
        color: theme.palette.error.dark,
    },
    increased: {
        color: theme.palette.success.dark,
    },
    dataDisplayItem: {
        marginTop: 0,
        marginBottom: 0,
        paddingTop: 1,
        paddingBottom: 1,
    },
    dataDisplayText: {
        marginTop: 0,
        marginBottom: 0,
    },
}));
