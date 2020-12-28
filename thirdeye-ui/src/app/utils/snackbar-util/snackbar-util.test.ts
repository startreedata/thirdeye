import {
    getErrorSnackbarOption,
    getInfoSnackbarOption,
    getSuccessSnackbarOption,
    getWarningSnackbarOption,
} from "./snackbar-util";

describe("Snackbar Util", () => {
    test("getErrorSnackbarOption shall return appropriate snackbar option with default prevent duplicate behavior", () => {
        const snackbarOption = getErrorSnackbarOption();

        expect(snackbarOption.variant).toEqual("error");
        expect(snackbarOption.preventDuplicate).toBeFalsy();
    });

    test("getErrorSnackbarOption shall return appropriate snackbar option", () => {
        let snackbarOption = getErrorSnackbarOption(true);

        expect(snackbarOption.variant).toEqual("error");
        expect(snackbarOption.preventDuplicate).toBeTruthy();

        snackbarOption = getErrorSnackbarOption(false);

        expect(snackbarOption.variant).toEqual("error");
        expect(snackbarOption.preventDuplicate).toBeFalsy();
    });

    test("getSuccessSnackbarOption shall return appropriate snackbar option with default prevent duplicate behavior", () => {
        const snackbarOption = getSuccessSnackbarOption();

        expect(snackbarOption.variant).toEqual("success");
        expect(snackbarOption.preventDuplicate).toBeFalsy();
    });

    test("getSuccessSnackbarOption shall return appropriate snackbar option", () => {
        let snackbarOption = getSuccessSnackbarOption(true);

        expect(snackbarOption.variant).toEqual("success");
        expect(snackbarOption.preventDuplicate).toBeTruthy();

        snackbarOption = getSuccessSnackbarOption(false);

        expect(snackbarOption.variant).toEqual("success");
        expect(snackbarOption.preventDuplicate).toBeFalsy();
    });

    test("getWarningSnackbarOption shall return appropriate snackbar option with default prevent duplicate behavior", () => {
        const snackbarOption = getWarningSnackbarOption();

        expect(snackbarOption.variant).toEqual("warning");
        expect(snackbarOption.preventDuplicate).toBeFalsy();
    });

    test("getWarningSnackbarOption shall return appropriate snackbar option", () => {
        let snackbarOption = getWarningSnackbarOption(true);

        expect(snackbarOption.variant).toEqual("warning");
        expect(snackbarOption.preventDuplicate).toBeTruthy();

        snackbarOption = getWarningSnackbarOption(false);

        expect(snackbarOption.variant).toEqual("warning");
        expect(snackbarOption.preventDuplicate).toBeFalsy();
    });

    test("getInfoSnackbarOption shall return appropriate snackbar option with default prevent duplicate behavior", () => {
        const snackbarOption = getInfoSnackbarOption();

        expect(snackbarOption.variant).toEqual("info");
        expect(snackbarOption.preventDuplicate).toBeFalsy();
    });

    test("getInfoSnackbarOption shall return appropriate snackbar option", () => {
        let snackbarOption = getInfoSnackbarOption(true);

        expect(snackbarOption.variant).toEqual("info");
        expect(snackbarOption.preventDuplicate).toBeTruthy();

        snackbarOption = getInfoSnackbarOption(false);

        expect(snackbarOption.variant).toEqual("info");
        expect(snackbarOption.preventDuplicate).toBeFalsy();
    });
});
