// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import {
    CircularProgress,
    FormControl,
    FormHelperText,
    useTheme,
} from "@material-ui/core";
import CancelIcon from "@material-ui/icons/Cancel";
import CheckCircleIcon from "@material-ui/icons/CheckCircle";
import classNames from "classnames";
import { Editor, EditorChange } from "codemirror";
import "codemirror/addon/edit/closebrackets.js";
import "codemirror/addon/edit/matchbrackets.js";
import "codemirror/addon/fold/brace-fold.js";
import "codemirror/addon/fold/foldgutter.css";
import "codemirror/addon/fold/foldgutter.js";
import "codemirror/addon/selection/active-line.js";
import "codemirror/lib/codemirror.css";
import "codemirror/mode/javascript/javascript.js";
import { debounce } from "lodash";
import React, { ReactElement, useCallback, useEffect, useState } from "react";
import { Controlled as CodeMirror } from "react-codemirror2";
import { JSONEditorV1Props } from "./json-editor-v1.interfaces";
import { useJSONEditorV1Styles } from "./json-editor-v1.styles";

const TAB_SIZE_JSON_EDITOR = 2;
const DELAY_VALIDATION_DEFAULT = 500;

export function JSONEditorV1<T = string>({
    value,
    helperText,
    error,
    readOnly,
    disableAutoFormat,
    allowEmpty,
    disableValidation,
    validationDelay = DELAY_VALIDATION_DEFAULT,
    className,
    onChange,
    onValidate,
    ...otherProps
}: JSONEditorV1Props<T>): ReactElement {
    const jsonEditorV1Classes = useJSONEditorV1Styles();
    const [jsonValue, setJSONValue] = useState("");
    const [validating, setValidating] = useState(true);
    const [valid, setValid] = useState(true);
    const theme = useTheme();

    useEffect(() => {
        // Input value changed, initialize JSON value
        initializeJSONValue();
    }, [value]);

    useEffect(() => {
        if (disableValidation) {
            setValidating(false);

            return;
        }

        // JSON value changed, validate
        setValidating(true);
        validateJSONValueDebounced(jsonValue);
    }, [jsonValue]);

    const initializeJSONValue = (): void => {
        if (!value) {
            setJSONValue("");

            return;
        }

        if (typeof value === "string" && disableAutoFormat) {
            setJSONValue(value);

            return;
        }

        if (typeof value === "string" && !disableAutoFormat) {
            // First, validate
            let jsonObject;
            try {
                jsonObject = JSON.parse(value);
            } catch (error) {
                // Invalid JSON
                setJSONValue(value);

                return;
            }

            setJSONValue(
                JSON.stringify(jsonObject, null, TAB_SIZE_JSON_EDITOR)
            );

            return;
        }

        if (typeof value === "object" && disableAutoFormat) {
            setJSONValue(JSON.stringify(value));

            return;
        }

        if (typeof value === "object" && !disableAutoFormat) {
            setJSONValue(JSON.stringify(value, null, TAB_SIZE_JSON_EDITOR));

            return;
        }
    };

    const validateJSONValueDebounced = useCallback(
        debounce((jsonValue) => {
            let valid = true;
            if (!jsonValue && !allowEmpty) {
                // Invalid JSON
                valid = false;
            }

            try {
                jsonValue && JSON.parse(jsonValue);
            } catch (error) {
                // Invalid JSON
                valid = false;
            }

            setValid(valid);
            setValidating(false);

            // Notify
            onValidate && onValidate(valid);
        }, validationDelay),
        []
    );

    const showValidationLoading = (): boolean => {
        return !disableValidation && validating;
    };

    const showValidationIcon = (): boolean => {
        return !disableValidation && !validating;
    };

    const handleInputBeforeChange = (
        _editor: Editor,
        _data: EditorChange,
        value: string
    ): void => {
        setJSONValue(value);
    };

    const handleInputChange = (
        _editor: Editor,
        _data: EditorChange,
        value: string
    ): void => {
        onChange && onChange(value);
    };

    return (
        <FormControl
            {...otherProps}
            className={classNames(
                jsonEditorV1Classes.jsonEditor,
                className,
                "json-editor-v1"
            )}
        >
            {/* CodeMirror */}
            <CodeMirror
                className={classNames(
                    jsonEditorV1Classes.codeMirror,
                    {
                        [jsonEditorV1Classes.codeMirrorError]: error || !valid,
                    },
                    "json-editor-v1-codemirror"
                )}
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
                    readOnly: readOnly,
                }}
                value={jsonValue}
                onBeforeChange={handleInputBeforeChange}
                onChange={handleInputChange}
            />

            {/* Validation in progress */}
            {showValidationLoading() && (
                <div
                    className={classNames(
                        jsonEditorV1Classes.validationIcon,
                        "json-editor-v1-validation-loading-indicator"
                    )}
                >
                    <CircularProgress color="primary" size={20} />
                </div>
            )}

            {/* Valid icon */}
            {showValidationIcon() && !error && valid && (
                <div
                    className={classNames(
                        jsonEditorV1Classes.validationIcon,
                        "json-editor-v1-valid-icon"
                    )}
                >
                    <CheckCircleIcon
                        fontSize="medium"
                        htmlColor={theme.palette.success.main}
                    />
                </div>
            )}

            {/* Invalid icon */}
            {showValidationIcon() && (error || !valid) && (
                <div
                    className={classNames(
                        jsonEditorV1Classes.validationIcon,
                        "json-editor-v1-invalid-icon"
                    )}
                >
                    <CancelIcon
                        fontSize="medium"
                        htmlColor={theme.palette.error.main}
                    />
                </div>
            )}

            {/* Helper text */}
            {helperText && (
                <FormHelperText
                    className="json-editor-v1-helper-text"
                    error={error || !valid}
                >
                    {helperText}
                </FormHelperText>
            )}
        </FormControl>
    );
}
