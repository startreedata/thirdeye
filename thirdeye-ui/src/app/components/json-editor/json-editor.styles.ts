import { makeStyles, Theme } from "@material-ui/core";

export const useJSONEditorStyles = makeStyles((theme: Theme) => ({
    container: {
        // Override CodeMirror styles to better align with Material-UI theme
        "& .CodeMirror": {
            borderRadius: theme.shape.borderRadius,
        },
        "& .CodeMirror-selected": {
            background: "highlight",
        },
        "& .CodeMirror-foldmarker": {
            color: theme.palette.primary.main,
            textShadow: "none",
        },
    },
}));
