/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import i18n from "i18next";
import LanguageDetector from "i18next-browser-languagedetector";
import { Settings } from "luxon";
import numbro from "numbro";
import { initReactI18next } from "react-i18next";
import { registerLanguages } from "../numbro/numbro.util";
import { initLocale } from "./locale.util";

const systemLocale = Settings.defaultLocale;

jest.mock("../numbro/numbro.util", () => ({
    registerLanguages: jest.fn(),
}));

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

jest.mock("numbro", () => ({
    setLanguage: jest.fn(),
}));

describe("Locale Util", () => {
    afterAll(() => {
        // Restore locale
        Settings.defaultLocale = systemLocale;
    });

    it("initLocale should initialize appropriate locale", () => {
        initLocale();

        expect(registerLanguages).toHaveBeenCalled();
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
