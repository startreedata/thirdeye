import { makeStyles, Theme } from "@material-ui/core";
import { codeTypographyOptions } from "../../utils/material-ui-util/typography-util";

export const useJSONEditorStyles = makeStyles((theme: Theme) => ({
    container: {
        // Override CodeMirror styles to better align with Material-UI theme
        "& .CodeMirror": {
            ...codeTypographyOptions,
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
