import { InitOptions } from "i18next";
import en from "../../locale/languages/en-us.json";

// Returns i18next options
export const getInitOptions = (): InitOptions => {
    return {
        interpolation: {
            escapeValue: false, // XSS safety provided by React
        },
        lng: "en",
        missingKeyHandler: (_lng, _ns, key) =>
            console.error(`i18next: key not found "${key}"`),
        resources: {
            en: {
                translation: en,
            },
        },
        saveMissing: true, // Required for missing key handler
    };
};
