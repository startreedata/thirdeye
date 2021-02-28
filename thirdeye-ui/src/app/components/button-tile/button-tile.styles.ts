import { makeStyles } from "@material-ui/core";

export const useButtonTileStyles = makeStyles((theme) => ({
    buttonRoot: {
        height: 130,
        width: 220,
        textTransform: "none",
        backgroundColor: theme.palette.background.paper,
    },
    icon: {
        height: 80,
    },
}));
