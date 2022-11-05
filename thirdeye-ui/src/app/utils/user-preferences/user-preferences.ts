/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
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
