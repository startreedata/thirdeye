import {
    getErrorSnackbarOption,
    getInfoSnackbarOption,
    getSuccessSnackbarOption,
    getWarningSnackbarOption,
} from "./snackbar.util";

describe("Snackbar Util", () => {
    test("getSuccessSnackbarOption should return appropriate snackbar option for default prevent duplicate behavior", () => {
        expect(getSuccessSnackbarOption()).toEqual({
            variant: "success",
            preventDuplicate: false,
        });
    });

    test("getSuccessSnackbarOption should return appropriate snackbar option for prevent duplicate behavior", () => {
        expect(getSuccessSnackbarOption(true)).toEqual({
            variant: "success",
            preventDuplicate: true,
        });
        expect(getSuccessSnackbarOption(false)).toEqual({
            variant: "success",
            preventDuplicate: false,
        });
    });

    test("getErrorSnackbarOption should return appropriate snackbar option for default prevent duplicate behavior", () => {
        expect(getErrorSnackbarOption()).toEqual({
            variant: "error",
            preventDuplicate: false,
        });
    });

    test("getErrorSnackbarOption should return appropriate snackbar option for prevent duplicate behavior", () => {
        expect(getErrorSnackbarOption(true)).toEqual({
            variant: "error",
            preventDuplicate: true,
        });
        expect(getErrorSnackbarOption(false)).toEqual({
            variant: "error",
            preventDuplicate: false,
        });
    });

    test("getWarningSnackbarOption should return appropriate snackbar option for default prevent duplicate behavior", () => {
        expect(getWarningSnackbarOption()).toEqual({
            variant: "warning",
            preventDuplicate: false,
        });
    });

    test("getWarningSnackbarOption should return appropriate snackbar option for prevent duplicate behavior", () => {
        expect(getWarningSnackbarOption(true)).toEqual({
            variant: "warning",
            preventDuplicate: true,
        });
        expect(getWarningSnackbarOption(false)).toEqual({
            variant: "warning",
            preventDuplicate: false,
        });
    });

    test("getInfoSnackbarOption should return appropriate snackbar option for default prevent duplicate behavior", () => {
        expect(getInfoSnackbarOption()).toEqual({
            variant: "info",
            preventDuplicate: false,
        });
    });

    test("getInfoSnackbarOption should return appropriate snackbar option for prevent duplicate behavior", () => {
        expect(getInfoSnackbarOption(true)).toEqual({
            variant: "info",
            preventDuplicate: true,
        });
        expect(getInfoSnackbarOption(false)).toEqual({
            variant: "info",
            preventDuplicate: false,
        });
    });
});
