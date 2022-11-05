import { makeStyles } from "@material-ui/core";

export const useTreemapStyles = makeStyles((theme) => ({
    heading: {
        fontSize: "13px",
        fontWeight: "bold",
    },
    clickable: {
        cursor: "pointer",
    },
    headingOtherDimension: {
        fontSize: "13px",
        fontWeight: "bold",
    },
    time: {
        marginBottom: theme.spacing(1),
    },
    alertEvaluationTimeSeriesTooltip: {
        minWidth: 140,
    },
    nameValueContents: {
        width: "100%",
        display: "table",
    },
    textOverflowEllipses: {
        whiteSpace: "nowrap",
        overflow: "hidden",
        textOverflow: "ellipsis",
    },
}));
