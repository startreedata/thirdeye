// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import { act, renderHook } from "@testing-library/react-hooks";
import { STORED_PREFERENCES_KEY, useUserPreferences } from "./user-preferences";
import { UserPreferencesKeys } from "./user-preferences.interfaces";

describe("User Preferences (useUserPreferences)", () => {
    beforeEach(() => {
        window.localStorage.clear();
    });

    it("should return undefined if there are no stored values", () => {
        const { result } = renderHook(() => useUserPreferences());

        expect(
            result.current.getPreference(
                UserPreferencesKeys.SHOW_DOCUMENTATION_RESOURCES
            )
        ).toBeUndefined();
    });

    it("should return expected value if preferences are stored in localStorage", () => {
        window.localStorage.setItem(
            STORED_PREFERENCES_KEY,
            JSON.stringify({
                [UserPreferencesKeys.SHOW_DOCUMENTATION_RESOURCES]: true,
            })
        );
        const { result } = renderHook(() => useUserPreferences());

        expect(
            result.current.getPreference(
                UserPreferencesKeys.SHOW_DOCUMENTATION_RESOURCES
            )
        ).toEqual(true);
    });

    it("should set value in state and localStorage", () => {
        const { result } = renderHook(() => useUserPreferences());

        act(() => {
            result.current.setPreference(
                UserPreferencesKeys.SHOW_DOCUMENTATION_RESOURCES,
                false
            );
        });

        expect(
            result.current.localPreferences[
                UserPreferencesKeys.SHOW_DOCUMENTATION_RESOURCES
            ]
        ).toEqual(false);
        expect(
            JSON.parse(
                window.localStorage.getItem(STORED_PREFERENCES_KEY) as string
            )[UserPreferencesKeys.SHOW_DOCUMENTATION_RESOURCES]
        ).toEqual(false);
    });
});
