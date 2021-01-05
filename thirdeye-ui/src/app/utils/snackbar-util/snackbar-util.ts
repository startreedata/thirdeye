import { OptionsObject, VariantType } from "notistack";

export enum SnackbarOption {
    ERROR = "ERROR",
    SUCCESS = "SUCCESS",
    WARNING = "WARNING",
    INFO = "INFO",
}

export const getErrorSnackbarOption = (
    preventDuplicate?: boolean
): OptionsObject => {
    return getSnackbarOption(SnackbarOption.ERROR, preventDuplicate);
};

export const getSuccessSnackbarOption = (
    preventDuplicate?: boolean
): OptionsObject => {
    return getSnackbarOption(SnackbarOption.SUCCESS, preventDuplicate);
};

export const getWarningSnackbarOption = (
    preventDuplicate?: boolean
): OptionsObject => {
    return getSnackbarOption(SnackbarOption.WARNING, preventDuplicate);
};

export const getInfoSnackbarOption = (
    preventDuplicate?: boolean
): OptionsObject => {
    return getSnackbarOption(SnackbarOption.INFO, preventDuplicate);
};

const getSnackbarOption = (
    snackbarOption: SnackbarOption,
    preventDuplicate?: boolean
): OptionsObject => {
    return {
        variant: snackbarOption.toLowerCase() as VariantType,
        preventDuplicate: Boolean(preventDuplicate),
    };
};
