import { makeStyles } from "@material-ui/core";
import { BorderV1 } from "../../utils/material-ui/border.util";
import { codeTypographyOptionsV1 } from "../../utils/material-ui/typography.util";

export const useJSONEditorV1Styles = makeStyles((theme) => ({
    jsonEditor: {
        height: "100%",
        width: "100%",
    },
    codeMirror: {
        height: "100%",
        width: "100%",
        border: BorderV1.BorderDefault,
        borderRadius: theme.shape.borderRadius,
        "& .CodeMirror": {
            height: "100%",
            width: "100%",
            ...codeTypographyOptionsV1.body2,
            borderRadius: theme.shape.borderRadius,
        },
        "& .CodeMirror-focused .CodeMirror-selected": {
            background: "highlight",
        },
        "& .CodeMirror-foldmarker": {
            color: theme.palette.primary.main,
            textShadow: "none",
        },
        "& .CodeMirror-matchingbracket": {
            color: `${theme.palette.primary.main} !important`,
            fontWeight: "bold",
        },
    },
    codeMirrorError: {
        borderColor: theme.palette.error.main,
    },
    validationIcon: {
        position: "absolute",
        top: 0,
        right: 0,
        margin: theme.spacing(1),
        zIndex: theme.zIndex.appBar - 1, // Validation icon to float above CodeMirror
    },
}));
