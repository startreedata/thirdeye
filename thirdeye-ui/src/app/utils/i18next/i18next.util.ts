import { InitOptions } from "i18next";
import enUS from "../../locale/languages/en-us.json";

// Returns i18next options
export const getInitOptions = (): InitOptions => {
    return {
        supportedLngs: ["en-US"],
        resources: {
            "en-US": { translation: enUS },
        },
        fallbackLng: ["en-US"],
        interpolation: {
            escapeValue: false, // XSS safety provided by React
        },
        missingKeyHandler: (_lngs, _ns, key) =>
            console.error(`i18next: key not found "${key}"`),
        saveMissing: true, // Required for missing key handler
    };
};
