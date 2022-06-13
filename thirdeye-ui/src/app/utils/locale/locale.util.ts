import i18n from "i18next";
import LanguageDetector from "i18next-browser-languagedetector";
import { Settings } from "luxon";
import numbro from "numbro";
import { initReactI18next } from "react-i18next";
import { getInitOptions } from "../i18next/i18next.util";

export const initLocale = (): void => {
    // Initialize i18next (language)
    i18n.use(LanguageDetector) // Detects system language
        .use(initReactI18next)
        .init(getInitOptions(), () => {
            // Force numbro (number) and Luxon (date/time) to use the same language
            numbro.setLanguage(i18n.language);
            Settings.defaultLocale = i18n.language;
        });
};
