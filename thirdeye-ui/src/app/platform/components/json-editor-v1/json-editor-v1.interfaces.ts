// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
export interface JSONEditorV1Props<T = string> {
    value?: T;
    helperText?: string;
    error?: boolean;
    readOnly?: boolean;
    hideValidationSuccessIcon?: boolean;
    disableAutoFormat?: boolean;
    allowEmpty?: boolean;
    disableValidation?: boolean;
    validationDelay?: number;
    className?: string;
    onChange?: (value: string) => void;
    onValidate?: (valid: boolean) => void;
}
