import { makeStyles, Theme } from "@material-ui/core";

export const useTextHighlighterStyles = makeStyles((theme: Theme) => ({
    highlight: {
        color: theme.palette.text.primary,
        backgroundColor: theme.palette.primary.main,
    },
}));
