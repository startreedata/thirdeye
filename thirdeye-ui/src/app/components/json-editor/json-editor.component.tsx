import { FormHelperText } from "@material-ui/core";
import { AppLoadingIndicatorV1 } from "@startree-ui/platform-ui";
import classnames from "classnames";
import { Editor, EditorChange } from "codemirror";
import "codemirror/addon/edit/closebrackets.js";
import "codemirror/addon/edit/matchbrackets.js";
import "codemirror/addon/fold/brace-fold";
import "codemirror/addon/fold/foldgutter.css";
import "codemirror/addon/fold/foldgutter.js";
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
import { JSONEditorProps } from "./json-editor.interfaces";
import { useJSONEditorStyles } from "./json-editor.styles";

const CodeMirror = lazy(() =>
    import(
        /* webpackChunkName: "react-code-mirror-2" */ "react-codemirror2"
    ).then((module) => ({ default: module.Controlled }))
);

const TAB_SIZE_JSON_EDITOR = 2;

export const JSONEditor: FunctionComponent<JSONEditorProps> = (
    props: JSONEditorProps
) => {
    const jsonEditorClasses = useJSONEditorStyles();
    const [value, setValue] = useState("");

    useEffect(() => {
        // Input value changed, initialize JSON value
        initJSONValue();
    }, [props.value]);

    const initJSONValue = (): void => {
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
            setValue(JSON.stringify(jsonObject, null, TAB_SIZE_JSON_EDITOR));

            return;
        }

        if (typeof props.value === "object") {
            setValue(JSON.stringify(props.value, null, TAB_SIZE_JSON_EDITOR));

            return;
        }

        setValue("");
    };

    const handleEditorInputBeforeChange = (
        _editor: Editor,
        _data: EditorChange,
        value: string
    ): void => {
        setValue(value);
    };

    const handleEditorInputChange = (
        _editor: Editor,
        _data: EditorChange,
        value: string
    ): void => {
        props.onChange && props.onChange(value);
    };

    return (
        <div className={props.className}>
            {/* JSON Editor */}
            <div
                className={classnames(jsonEditorClasses.jsonEditor, {
                    [jsonEditorClasses.jsonEditorWithHelperText]:
                        props.helperText,
                    [jsonEditorClasses.jsonEditorWithoutHelperText]: !props.helperText,
                    [jsonEditorClasses.jsonEditorDefaultBorder]: !props.error,
                    [jsonEditorClasses.jsonEditorErrorBorder]: props.error,
                })}
            >
                {/* CodeMirror */}
                <Suspense fallback={<AppLoadingIndicatorV1 />}>
                    <CodeMirror
                        className={jsonEditorClasses.codeMirror}
                        options={{
                            tabSize: TAB_SIZE_JSON_EDITOR,
                            indentUnit: TAB_SIZE_JSON_EDITOR,
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
                        }}
                        value={value}
                        onBeforeChange={handleEditorInputBeforeChange}
                        onChange={handleEditorInputChange}
                    />
                </Suspense>
            </div>

            {/* Helper text */}
            {props.helperText && (
                <FormHelperText
                    className={jsonEditorClasses.helperText}
                    error={props.error}
                >
                    {props.helperText}
                </FormHelperText>
            )}
        </div>
    );
};
