import { makeStyles } from "@material-ui/core";

export const useAnomalyQuickFilterStyles = makeStyles((theme) => ({
    root: {
        display: "flex",
        flexWrap: "wrap",
        alignItems: "center",
        width: "100%",
        justifyContent: "space-between",
    },
    dataGridToolbarSearch: {
        width: 170,
        marginLeft: theme.spacing(1),
    },
    filterGroup: {
        display: "flex",
        flexWrap: "wrap",
    },
}));
