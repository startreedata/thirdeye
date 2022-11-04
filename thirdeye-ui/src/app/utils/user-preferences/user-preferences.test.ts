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
