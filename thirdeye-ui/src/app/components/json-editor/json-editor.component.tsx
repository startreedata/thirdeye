import { Box, FormHelperText, useTheme } from "@material-ui/core";
import { Editor, EditorChange, EditorConfiguration } from "codemirror";
import "codemirror/addon/edit/closebrackets.js";
import "codemirror/addon/edit/matchbrackets.js";
import "codemirror/addon/fold/brace-fold";
import "codemirror/addon/fold/foldgutter";
import "codemirror/addon/fold/foldgutter.css";
import "codemirror/addon/selection/active-line";
import "codemirror/lib/codemirror.css";
import "codemirror/mode/javascript/javascript";
import React, {
    FunctionComponent,
    lazy,
    Suspense,
    useEffect,
    useState,
} from "react";
import { Dimension } from "../../utils/material-ui/dimension.util";
import { Palette } from "../../utils/material-ui/palette.util";
import { LoadingIndicator } from "../loading-indicator/loading-indicator.component";
import { JSONEditorProps } from "./json-editor.interfaces";
import { useJSONEditorStyles } from "./json-editor.styles";

const CodeMirror = lazy(() =>
    import(
        /* webpackChunkName: "react-code-mirror-2" */ "react-codemirror2"
    ).then((module) => ({ default: module.Controlled }))
);

const TAB_SIZE = 2;

export const JSONEditor: FunctionComponent<JSONEditorProps> = (
    props: JSONEditorProps
) => {
    const jsonEditorClasses = useJSONEditorStyles();
    const [value, setValue] = useState("");
    const [mouseHover, setMouseHover] = useState(false);
    const theme = useTheme();

    useEffect(() => {
        // Input changed, reset
        initJSONInput();
    }, [props.value]);

    const onEditorMouseEnter = (): void => {
        setMouseHover(true);
    };

    const onEditorMouseLeave = (): void => {
        setMouseHover(false);
    };

    const onBeforeCodeMirrorInputChange = (
        _editor: Editor,
        _data: EditorChange,
        value: string
    ): void => {
        setValue(value);
    };

    const onCodeMirrorInputChange = (
        _editor: Editor,
        _data: EditorChange,
        value: string
    ): void => {
        props.onChange && props.onChange(value);
    };

    const initJSONInput = (): void => {
        if (typeof props.value === "string") {
            // Format string if possible
            let jsonObject;
            try {
                jsonObject = JSON.parse(props.value);
            } catch (error) {
                // Invalid JSON, set string as is
                setValue(props.value);

                return;
            }

            // Valid JSON
            setValue(JSON.stringify(jsonObject, null, TAB_SIZE));

            return;
        }

        if (typeof props.value === "object") {
            setValue(JSON.stringify(props.value, null, TAB_SIZE));

            return;
        }

        setValue("");
    };

    return (
        <>
            {/* Editor */}
            <Box
                border={Dimension.WIDTH_BORDER_DEFAULT}
                borderColor={
                    props.error
                        ? theme.palette.error.main
                        : mouseHover
                        ? Palette.COLOR_BORDER_DARK
                        : Palette.COLOR_BORDER_DEFAULT
                }
                borderRadius={theme.shape.borderRadius}
                onMouseEnter={onEditorMouseEnter}
                onMouseLeave={onEditorMouseLeave}
            >
                <Suspense fallback={<LoadingIndicator />}>
                    <CodeMirror
                        className={jsonEditorClasses.container}
                        options={
                            {
                                tabSize: 2,
                                indentUnit: 2,
                                indentWithTabs: false,
                                lineNumbers: true,
                                lineWrapping: true,
                                styleActiveLine: true,
                                matchBrackets: true,
                                autoCloseBrackets: true,
                                foldGutter: true,
                                gutters: [
                                    "CodeMirror-linenumbers",
                                    "CodeMirror-foldgutter",
                                ],
                                mode: {
                                    name: "javascript",
                                    json: true,
                                },
                                readOnly: props.readOnly,
                            } as EditorConfiguration
                        }
                        value={value}
                        onBeforeChange={onBeforeCodeMirrorInputChange}
                        onChange={onCodeMirrorInputChange}
                    />
                </Suspense>
            </Box>

            {/* Helper text */}
            <FormHelperText error={props.error}>
                {props.helperText || ""}
            </FormHelperText>
        </>
    );
};
