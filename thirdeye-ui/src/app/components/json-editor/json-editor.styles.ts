import { makeStyles } from "@material-ui/core";
import { codeTypographyOptions } from "../../utils/material-ui/typography.util";

export const useJSONEditorStyles = makeStyles((theme) => ({
    editor: {
        // Override CodeMirror styles to better align with Material-UI theme
        "& .CodeMirror": {
            ...codeTypographyOptions.body2,
            borderRadius: theme.shape.borderRadius,
        },
        "& .CodeMirror-focused .CodeMirror-selected": {
            background: "highlight",
        },
        "& .CodeMirror-foldmarker": {
            color: theme.palette.primary.main,
            textShadow: "none",
        },
    },
}));
