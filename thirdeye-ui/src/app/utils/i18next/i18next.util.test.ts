import enUS from "../../locale/languages/en-us.json";
import { getInitOptions } from "./i18next.util";

describe("i18next Util", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });

    it("getInitOptions should return appropriate options", () => {
        jest.spyOn(console, "error").mockImplementation();
        const initOptions = getInitOptions();
        // Also invoke the missing key handler
        initOptions.missingKeyHandler &&
            initOptions.missingKeyHandler([""], "ns", "testKey", "");

        expect(initOptions.supportedLngs).toEqual(["en-US"]);
        expect(
            initOptions.resources && initOptions.resources["en-US"].translation
        ).toEqual(enUS);
        expect(initOptions.fallbackLng).toEqual(["en-US"]);
        expect(
            initOptions.interpolation && initOptions.interpolation.escapeValue
        ).toBeFalsy();
        expect(console.error).toHaveBeenCalledWith(
            `i18next: key not found "testKey"`
        );
        expect(initOptions.saveMissing).toBeTruthy();
    });
});
