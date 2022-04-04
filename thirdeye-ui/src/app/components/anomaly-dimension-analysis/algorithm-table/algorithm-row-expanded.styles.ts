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
        backgroundColor: "rgb(241,242,255)",
    },
    expandedRow: {
        paddingTop: theme.spacing(1),
        paddingBottom: theme.spacing(1),
        backgroundColor: "rgba(247, 249, 255)",
    },
}));
