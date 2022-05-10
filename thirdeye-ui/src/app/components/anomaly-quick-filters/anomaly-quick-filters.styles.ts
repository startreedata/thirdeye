import { makeStyles } from "@material-ui/core";

export const useAnomalyQuickFilterStyles = makeStyles((theme) => ({
    root: {
        display: "flex",
        flexWrap: "wrap",
        "& > *": {
            marginRight: theme.spacing(1),
        },
    },
}));
