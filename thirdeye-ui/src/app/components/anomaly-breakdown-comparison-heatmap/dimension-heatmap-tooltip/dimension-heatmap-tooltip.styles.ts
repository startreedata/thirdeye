import { makeStyles } from "@material-ui/core";

export const useDimensionHeatmapTooltipStyles = makeStyles((theme) => ({
    spaceBottom: {
        marginBottom: theme.spacing(1),
    },
    dataDisplayList: {
        minWidth: "300px;",
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
