import i18n from "i18next";
import { initReactI18next } from "react-i18next";
import en from "../locale/en.json";

// Initializes i18next
export const initI18next = (): void => {
    i18n.use(initReactI18next).init({
        interpolation: {
            escapeValue: false, // XSS safety provided by React
        },
        lng: "en",
        missingKeyHandler: (_lng: string[], _ns: string, key: string): void => {
            // Key not found
            console.error(`i18next: key not found "${key}"`);
        },
        resources: {
            en: {
                translation: en,
            },
        },
        saveMissing: true, // Required for missingKeyHandler
    });
};
