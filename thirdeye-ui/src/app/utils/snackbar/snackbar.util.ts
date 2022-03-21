import { OptionsObject, VariantType } from "notistack";

export const getSuccessSnackbarOption = (
    preventDuplicate?: boolean
): OptionsObject => {
    return getSnackbarOption("success", preventDuplicate);
};

export const getErrorSnackbarOption = (
    preventDuplicate?: boolean
): OptionsObject => {
    return getSnackbarOption("error", preventDuplicate);
};

export const getWarningSnackbarOption = (
    preventDuplicate?: boolean
): OptionsObject => {
    return getSnackbarOption("warning", preventDuplicate);
};

export const getInfoSnackbarOption = (
    preventDuplicate?: boolean
): OptionsObject => {
    return getSnackbarOption("info", preventDuplicate);
};

const getSnackbarOption = (
    variant: VariantType,
    preventDuplicate?: boolean
): OptionsObject => {
    return {
        variant: variant,
        preventDuplicate: Boolean(preventDuplicate),
    };
};
