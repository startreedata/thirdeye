import { act, renderHook } from "@testing-library/react-hooks";
import { useAuthStore } from "./auth.store";

describe("Auth Store", () => {
    it("should initialize default values", () => {
        const { result } = renderHook(() => useAuthStore());

        expect(result.current.authDisabled).toBeFalsy();
        expect(result.current.authenticated).toBeFalsy();
        expect(result.current.accessToken).toEqual("");
        expect(result.current.redirectPath).toEqual("");
    });

    it("disableAuth should update store appropriately", () => {
        const { result } = renderHook(() => useAuthStore());
        act(() => {
            result.current.disableAuth();
        });

        expect(result.current.authDisabled).toBeTruthy();
        expect(result.current.authenticated).toBeFalsy();
        expect(result.current.accessToken).toEqual("");
        expect(result.current.redirectPath).toEqual("");
    });

    it("setAccessToken should update store appropriately for invalid token", () => {
        const { result } = renderHook(() => useAuthStore());
        act(() => {
            result.current.setAccessToken(null as unknown as string);
        });

        expect(result.current.authDisabled).toBeFalsy();
        expect(result.current.authenticated).toBeFalsy();
        expect(result.current.accessToken).toEqual("");
        expect(result.current.redirectPath).toEqual("");
    });

    it("setAccessToken should update store appropriately for empty token", () => {
        const { result } = renderHook(() => useAuthStore());
        act(() => {
            result.current.setAccessToken("");
        });

        expect(result.current.authDisabled).toBeFalsy();
        expect(result.current.authenticated).toBeFalsy();
        expect(result.current.accessToken).toEqual("");
        expect(result.current.redirectPath).toEqual("");
    });

    it("setAccessToken should update store appropriately for token", () => {
        const { result } = renderHook(() => useAuthStore());
        act(() => {
            result.current.setAccessToken("testToken");
        });

        expect(result.current.authDisabled).toBeFalsy();
        expect(result.current.authenticated).toBeTruthy();
        expect(result.current.accessToken).toEqual("testToken");
        expect(result.current.redirectPath).toEqual("");
    });

    it("clearAccessToken should update store appropriately", () => {
        const { result } = renderHook(() => useAuthStore());
        act(() => {
            result.current.clearAccessToken();
        });

        expect(result.current.authDisabled).toBeFalsy();
        expect(result.current.authenticated).toBeFalsy();
        expect(result.current.accessToken).toEqual("");
        expect(result.current.redirectPath).toEqual("");
    });

    it("setRedirectPath should update store appropriately for path", () => {
        const { result } = renderHook(() => useAuthStore());
        act(() => {
            result.current.setRedirectPath("testPath");
        });

        expect(result.current.authDisabled).toBeFalsy();
        expect(result.current.authenticated).toBeFalsy();
        expect(result.current.accessToken).toEqual("");
        expect(result.current.redirectPath).toEqual("testPath");
    });

    it("clearRedirectPath should update store appropriately", () => {
        const { result } = renderHook(() => useAuthStore());
        act(() => {
            result.current.clearRedirectPath();
        });

        expect(result.current.authDisabled).toBeFalsy();
        expect(result.current.authenticated).toBeFalsy();
        expect(result.current.accessToken).toEqual("");
        expect(result.current.redirectPath).toEqual("");
    });

    it("should persist in browser local storage", async () => {
        const { result, waitFor } = renderHook(() => useAuthStore());
        act(() => {
            result.current.setAccessToken("testToken");
            result.current.setRedirectPath("testPath");
        });
        await waitFor(() => Boolean(result.current.accessToken));

        expect(localStorage.getItem("LOCAL_STORAGE_KEY_AUTH")).toEqual(
            `{` +
                `"state":{` +
                `"authDisabled":false,` +
                `"authenticated":true,` +
                `"accessToken":"testToken",` +
                `"redirectPath":"testPath"` +
                `},` +
                `"version":0` +
                `}`
        );
    });
});
