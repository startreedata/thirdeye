import { makeStyles } from "@material-ui/core";

export const useMetricTileStyles = makeStyles((theme) => ({
    metricTile: {
        height: 130,
        width: 220,
    },
    metricTileCompact: {
        height: 90,
        width: 140,
    },
    metricTileContents: {
        height: "100%",
        width: "100%",
        display: "flex",
        flexDirection: "column",
        padding: theme.spacing(1),
    },
    metricValueContainer: {
        width: "100%",
        display: "flex",
        flex: 1,
        alignItems: "center",
        justifyContent: "center",
    },
    metricNameContainer: {
        minHeight: 30,
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
    },
    metricName: {
        lineHeight: 1.2,
        wordBreak: "break-word",
    },
}));
