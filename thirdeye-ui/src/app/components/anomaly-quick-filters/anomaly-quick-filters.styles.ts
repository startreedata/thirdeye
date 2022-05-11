import { makeStyles } from "@material-ui/core";

export const useAnomalyQuickFilterStyles = makeStyles((theme) => ({
    root: {
        display: "flex",
        flexWrap: "wrap",
    },
    dataGridToolbarSearch: {
        width: 225,
        marginLeft: theme.spacing(1),
    },
}));
