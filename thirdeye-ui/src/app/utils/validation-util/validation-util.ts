import i18n from "i18next";
import * as yup from "yup";

export interface ValidationResult {
    valid: boolean;
    message?: string;
}

export const validateEmail = (email: string): ValidationResult => {
    const validationResult = {
        valid: true,
        message: "",
    };

    const emailSchema = yup.object().shape({
        email: yup
            .string()
            .trim()
            .required(i18n.t("message.field-required"))
            .email(i18n.t("message.invalid-email")),
    });
    try {
        emailSchema.validateSync({ email: email });
    } catch (error) {
        validationResult.valid = false;
        validationResult.message = error && error.message;
    }

    return validationResult;
};
