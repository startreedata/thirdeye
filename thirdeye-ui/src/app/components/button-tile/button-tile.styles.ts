import { makeStyles } from "@material-ui/core";

export const useButtonTileStyles = makeStyles((theme) => ({
    buttonRoot: {
        height: 130,
        width: 220,
        textTransform: "none",
        backgroundColor: theme.palette.background.paper,
        padding: theme.spacing(1),
    },
    buttonLabel: {
        height: "100%",
        width: "100%",
        display: "flex",
        flexDirection: "column",
    },
    iconContainer: {
        display: "flex",
        flex: 1,
        alignItems: "center",
        justifyContent: "center",
    },
    textContainer: {
        minHeight: 30,
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
    },
    text: {
        lineHeight: 1.2,
        wordBreak: "break-word",
    },
}));
