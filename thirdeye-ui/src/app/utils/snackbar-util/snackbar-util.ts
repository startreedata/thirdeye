import { OptionsObject } from "notistack";

export const SnackbarOption = {
    ERROR: {
        variant: "error",
    } as OptionsObject,
    SUCCESS: {
        variant: "success",
    } as OptionsObject,
    WARNING: {
        variant: "warning",
    } as OptionsObject,
    INFO: {
        variant: "info",
    } as OptionsObject,
} as const;
