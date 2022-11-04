import { useCallback, useState } from "react";
import {
    UserPreferences,
    UseUserPreferencesHook,
} from "./user-preferences.interfaces";

export const STORED_PREFERENCES_KEY = "TE_USER_PREFERENCES";

export const useUserPreferences = (): UseUserPreferencesHook => {
    const [localPreferences, setLocalPreferences] = useState<UserPreferences>(
        () => {
            if (!window.localStorage) {
                return {};
            }
            const storedPreferenceJson = window.localStorage.getItem(
                STORED_PREFERENCES_KEY
            );

            if (storedPreferenceJson !== null) {
                return JSON.parse(storedPreferenceJson);
            }

            return {};
        }
    );

    const setPreference = useCallback(
        (prefKey: keyof UserPreferences, prefValue) => {
            setLocalPreferences((current) => {
                const cloned = { ...current };
                cloned[prefKey] = prefValue;

                if (window.localStorage) {
                    window.localStorage.setItem(
                        STORED_PREFERENCES_KEY,
                        JSON.stringify(cloned)
                    );
                }

                return cloned;
            });
        },
        []
    );
    const getPreference = useCallback((prefKey: keyof UserPreferences) => {
        return localPreferences[prefKey];
    }, []);

    return { setPreference, getPreference, localPreferences };
};
