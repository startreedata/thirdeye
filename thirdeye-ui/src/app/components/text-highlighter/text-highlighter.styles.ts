import { makeStyles } from "@material-ui/core";

export const useTextHighlighterStyles = makeStyles((theme) => ({
    highlight: {
        color: theme.palette.primary.contrastText,
        backgroundColor: theme.palette.primary.main,
    },
}));
