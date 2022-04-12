import { makeStyles } from "@material-ui/core";

export const useAlgorithmRowExpandedStyles = makeStyles((theme) => ({
    /**
     * Make the row on top of the one with expanded content blend by removing
     * the bottom border
     */
    root: {
        "& > *": {
            borderBottom: "unset",
        },
    },
    expandedRowParent: {
        boxShadow:
            "rgba(60, 64, 67, 0.3) 0px 1px 2px 0px, rgba(60, 64, 67, 0.15) 0px 1px 3px 1px;",
        backgroundColor: "rgb(250, 250, 250)",
    },
    expandedRow: {
        paddingTop: theme.spacing(1),
        paddingBottom: theme.spacing(1),
    },
    closedRow: {
        padding: 0,
    },
}));
