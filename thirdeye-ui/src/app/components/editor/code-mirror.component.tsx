import { makeStyles } from "@material-ui/core";
import { Editor, EditorChange } from "codemirror";
import "codemirror/addon/hint/show-hint";
import "codemirror/addon/hint/show-hint.css";
import "codemirror/lib/codemirror.css";
import React, { FunctionComponent, lazy } from "react";
import { CommonCodeMirrorProps } from "./code-mirror.interfaces";

const CodeMirror = lazy(() =>
    import(
        /* webpackChunkName: 'ReactCodeMirror' */
        "react-codemirror2"
    ).then(({ Controlled }) => ({
        default: Controlled,
    }))
);

const useStyles = makeStyles(() => {
    return {
        codemirror: {
            "& .CodeMirror": {
                border: "1px solid rgba(25, 25, 25, 0.38)",
                fontSize: "12px",
                fontFamily: "Roboto Slab !important",
                borderRadius: 8,
                fontWeight: 400,
            },
        },
    };
});

const CommonCodeMirror: FunctionComponent<CommonCodeMirrorProps> = ({
    options,
    value,
    onChange,
}: CommonCodeMirrorProps) => {
    const classes = useStyles();

    return (
        <CodeMirror
            className={classes.codemirror}
            options={options}
            value={value || ""}
            onBeforeChange={(
                _editor: Editor,
                _data: EditorChange,
                newValue: string
            ): void => {
                onChange && onChange(newValue);
            }}
        />
    );
};

export default CommonCodeMirror;
