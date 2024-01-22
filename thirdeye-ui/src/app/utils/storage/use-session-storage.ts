/*
 * Copyright 2024 StarTree Inc
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
import { useState } from "react";

export type UseSessionStorageReturn<T> = [T, (newVal: T) => void, () => void];

const getDefaultStoredValue = <T>(
    keyName: SessionStorageKeys,
    defaultValue: T
): T => {
    try {
        const value = window.sessionStorage.getItem(keyName);

        if (value) {
            return JSON.parse(value) as unknown as T;
        } else {
            window.sessionStorage.setItem(
                keyName,
                JSON.stringify(defaultValue)
            );

            return defaultValue;
        }
    } catch (err) {
        return defaultValue;
    }
};

export const useSessionStorage = <T = string>(
    keyName: SessionStorageKeys,
    defaultValue: T
): UseSessionStorageReturn<T> => {
    const [storedValue, setStoredValue] = useState<T>(
        getDefaultStoredValue(keyName, defaultValue)
    );

    const setValue = (newValue: T): void => {
        try {
            window.sessionStorage.setItem(keyName, JSON.stringify(newValue));
        } catch (err) {
            console.error(`Unable to save new value for ${keyName} to storage`);
        }
        setStoredValue(newValue);
    };

    const deleteValue = (): void => {
        try {
            window.sessionStorage.removeItem(keyName);
        } catch (err) {
            console.error(`Unable to delete value for ${keyName} from`);
        }
    };

    return [storedValue, setValue, deleteValue];
};

// Add keys here to use this hook. This centralized store ensures
// that values aren't unintentionally read / overwritten
export enum SessionStorageKeys {
    SelectedDimensionsOnAlertFlow = "SelectedDimensionsOnAlertFlow",
    QueryFilterOnAlertFlow = "QueryFilterOnAlertFlow",
}
