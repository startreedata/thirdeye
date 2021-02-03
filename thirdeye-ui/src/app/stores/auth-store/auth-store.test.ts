import { act, renderHook } from "@testing-library/react-hooks";
import { useAuthStore } from "./auth-store";

describe("Auth Store", () => {
    test("should initialize default values", () => {
        const { result } = renderHook(() => useAuthStore());

        expect(result.current.authDisabled).toBeFalsy();
        expect(result.current.authenticated).toBeFalsy();
        expect(result.current.accessToken).toEqual("");
    });

    test("disableAuth should update store appropriately", () => {
        const { result } = renderHook(() => useAuthStore());
        act(() => {
            result.current.disableAuth();
        });

        expect(result.current.authDisabled).toBeTruthy();
        expect(result.current.authenticated).toBeFalsy();
        expect(result.current.accessToken).toEqual("");
    });

    test("setAccessToken should update store appropriately for invalid token", () => {
        const { result } = renderHook(() => useAuthStore());
        act(() => {
            result.current.setAccessToken((null as unknown) as string);
        });

        expect(result.current.authDisabled).toBeFalsy();
        expect(result.current.authenticated).toBeFalsy();
        expect(result.current.accessToken).toEqual("");

        act(() => {
            result.current.setAccessToken("");
        });

        expect(result.current.authDisabled).toBeFalsy();
        expect(result.current.authenticated).toBeFalsy();
        expect(result.current.accessToken).toEqual("");
    });

    test("setAccessToken should update store appropriately for token", () => {
        const { result } = renderHook(() => useAuthStore());
        act(() => {
            result.current.setAccessToken("testToken");
        });

        expect(result.current.authDisabled).toBeFalsy();
        expect(result.current.authenticated).toBeTruthy();
        expect(result.current.accessToken).toEqual("testToken");
    });

    test("clearAccessToken should update store appropriately", () => {
        const { result } = renderHook(() => useAuthStore());
        act(() => {
            result.current.clearAccessToken();
        });

        expect(result.current.authDisabled).toBeFalsy();
        expect(result.current.authenticated).toBeFalsy();
        expect(result.current.accessToken).toEqual("");
    });

    test("should persist in local storage", async () => {
        const { result, waitFor } = renderHook(() => useAuthStore());
        act(() => {
            result.current.setAccessToken("testToken");
        });

        await waitFor(() => Boolean(result.current.accessToken));

        expect(localStorage.getItem("LOCAL_STORAGE_KEY_AUTH")).toEqual(
            `{` +
                `"state":{` +
                `"authDisabled":false,` +
                `"authenticated":true,` +
                `"accessToken":"testToken"` +
                `},` +
                `"version":0` +
                `}`
        );
    });
});
