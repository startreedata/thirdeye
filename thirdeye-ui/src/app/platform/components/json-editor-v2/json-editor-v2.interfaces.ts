/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
export interface JSONEditorV2Props<T = string> {
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
    actions: Array<{
        label: string;
        onClick: () => void;
    }>;
    showFooter?: boolean;
    onChange?: (value: string) => void;
    onValidate?: (valid: boolean) => void;
}
