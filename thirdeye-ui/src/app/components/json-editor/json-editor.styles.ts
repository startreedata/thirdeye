import { makeStyles } from "@material-ui/core";
import { codeTypographyOptions } from "../../utils/material-ui/typography.util";

const HEIGHT_JSON_EDITOR_ERROR = 20;

export const useJSONEditorStyles = makeStyles((theme) => ({
    jsonEditorContainer: {
        height: "100%",
        width: "100%",
    },
    jsonEditorContainerWithError: {
        height: `calc(100% - ${HEIGHT_JSON_EDITOR_ERROR}px)`,
        width: "100%",
    },
    jsonEditor: {
        height: "100%",
        width: "100%",
        // Override CodeMirror styles to better align with Material-UI theme
        "& .CodeMirror": {
            height: "100%",
            width: "100%",
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
    error: {
        height: HEIGHT_JSON_EDITOR_ERROR,
    },
}));
