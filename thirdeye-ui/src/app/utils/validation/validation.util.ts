///
/// Copyright 2022 StarTree Inc
///
/// Licensed under the StarTree Community License (the "License"); you may not use
/// this file except in compliance with the License. You may obtain a copy of the
/// License at http://www.startree.ai/legal/startree-community-license
///
/// Unless required by applicable law or agreed to in writing, software distributed under the
/// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
/// either express or implied.
/// See the License for the specific language governing permissions and limitations under
/// the License.
///

import i18n from "i18next";
import * as yup from "yup";

export interface ValidationResult {
    valid: boolean;
    message?: string;
}

export const validateEmail = (email: string): ValidationResult => {
    const validationResult: ValidationResult = {
        valid: true,
    };

    const emailSchema = yup.object().shape({
        email: yup
            .string()
            .ensure()
            .trim()
            .required(i18n.t("message.email-required"))
            .email(i18n.t("message.invalid-email")),
    });
    try {
        emailSchema.validateSync({
            email: email,
        });
    } catch (error) {
        validationResult.valid = false;
        validationResult.message = error && error.message;
    }

    return validationResult;
};

export const validateJSON = (json: string): ValidationResult => {
    const validationResult: ValidationResult = {
        valid: true,
    };

    if (!json) {
        validationResult.valid = false;
        validationResult.message = i18n.t("message.json-input-required");

        return validationResult;
    }

    try {
        JSON.parse(json);
    } catch (error) {
        validationResult.valid = false;
        validationResult.message = i18n.t("message.invalid-json");
    }

    return validationResult;
};
