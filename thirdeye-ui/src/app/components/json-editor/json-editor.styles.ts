import { makeStyles } from "@material-ui/core";
import { Border } from "../../utils/material-ui/border.util";
import { codeTypographyOptions } from "../../utils/material-ui/typography.util";

const HEIGHT_HELPER_TEXT = 20;

export const useJSONEditorStyles = makeStyles((theme) => ({
    jsonEditor: {
        width: "100%",
        borderRadius: theme.shape.borderRadius,
    },
    jsonEditorWithHelperText: {
        height: `calc(100% - ${HEIGHT_HELPER_TEXT}px)`,
    },
    jsonEditorWithoutHelperText: {
        height: "100%",
    },
    jsonEditorDefaultBorder: {
        border: Border.BORDER_INPUT_DEFAULT,
    },
    jsonEditorErrorBorder: {
        border: Border.BORDER_INPUT_ERROR,
    },
    codeMirror: {
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
    helperText: {
        height: HEIGHT_HELPER_TEXT,
    },
}));
