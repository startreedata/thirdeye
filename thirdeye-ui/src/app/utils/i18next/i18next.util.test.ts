import { getInitOptions } from "./i18next.util";

describe("i18next Util", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });

    test("getInitOptions should return appropriate options", () => {
        jest.spyOn(console, "error").mockImplementation();
        const initOptions = getInitOptions();
        // Also invoke the missing key handler
        initOptions.missingKeyHandler &&
            initOptions.missingKeyHandler([""], "ns", "testKey", "");

        expect(initOptions.interpolation?.escapeValue).toBeFalsy();
        expect(initOptions.lng).toEqual("en");
        expect(console.error).toHaveBeenCalledWith(
            `i18next: key not found "testKey"`
        );
        expect(initOptions.resources?.en).toBeDefined();
        expect(initOptions.saveMissing).toBeTruthy();
    });
});
