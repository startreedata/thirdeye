import { InitOptions } from "i18next";
import en from "../../locale/languages/en-us.json";

// Returns i18next InitOptions
export const getInitOptions = (): InitOptions => {
    const initOptions: InitOptions = {
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
    };

    return initOptions;
};
