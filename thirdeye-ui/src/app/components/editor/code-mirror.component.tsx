import { makeStyles } from "@material-ui/core";
import { Editor, EditorChange, EditorConfiguration } from "codemirror";
import "codemirror/addon/hint/show-hint";
import "codemirror/addon/hint/show-hint.css";
import React, { FunctionComponent } from "react";
import { Controlled as CodeMirror } from "react-codemirror2";

type Props = {
    options: EditorConfiguration;
    value?: string;
    defaultValue?: string;
    register?: (ref: Element | null) => void;
    name?: string;
    onChange?: (value: string) => void;
};

const useStyles = makeStyles(() => {
    return {
        codemirror: {
            border: "1px solid rgba(25, 25, 25, 0.38)",
            fontSize: "12px",
            fontFamily: "Roboto Slab !important",
            fontWeight: 400,
        },
    };
});

const CommonCodeMirror: FunctionComponent<Props> = ({
    options,
    value,
    onChange,
}: Props) => {
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
