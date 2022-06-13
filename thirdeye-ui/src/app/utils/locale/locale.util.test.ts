import i18n from "i18next";
import LanguageDetector from "i18next-browser-languagedetector";
import { Settings } from "luxon";
import numbro from "numbro";
import { initReactI18next } from "react-i18next";
import { initLocale } from "./locale.util";

const systemLocale = Settings.defaultLocale;

jest.mock("i18next", () => ({
    use: jest.fn().mockReturnThis(),
    init: jest.fn().mockImplementation((_initOptions, callback) => callback()),
    language: "testLanguage",
}));

jest.mock("i18next-browser-languagedetector", () => ({
    LanguageDetector: {},
}));

jest.mock("react-i18next", () => ({
    initReactI18next: {},
}));

jest.mock("../i18next/i18next.util", () => ({
    getInitOptions: jest.fn().mockImplementation(() => mockInitOptions),
}));

describe("Locale Util", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });

    afterAll(() => {
        // Restore locale
        Settings.defaultLocale = systemLocale;
    });

    it("initLocale should initialize appropriate locale", () => {
        jest.spyOn(numbro, "setLanguage").mockImplementation();
        initLocale();

        expect(i18n.use).toHaveBeenNthCalledWith(1, LanguageDetector);
        expect(i18n.use).toHaveBeenNthCalledWith(2, initReactI18next);
        expect(i18n.init).toHaveBeenCalledWith(
            mockInitOptions,
            expect.any(Function)
        );
        expect(numbro.setLanguage).toHaveBeenCalledWith("testLanguage");
        expect(Settings.defaultLocale).toEqual("testLanguage");
    });
});

const mockInitOptions = {};
