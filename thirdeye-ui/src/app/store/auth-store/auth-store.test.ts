import { act, renderHook } from "@testing-library/react-hooks";
import { useAuthStore } from "./auth-store";

describe("Auth Store", () => {
    test("should initialize default values", () => {
        const { result } = renderHook(() => useAuthStore());

        expect(result.current.auth).toBeFalsy();
        expect(result.current.accessToken).toEqual("");
    });

    test("setAccessToken should update store appropriately for invalid token", () => {
        const { result } = renderHook(() => useAuthStore());
        act(() => {
            result.current.setAccessToken((null as unknown) as string);
        });

        expect(result.current.auth).toBeFalsy();
        expect(result.current.accessToken).toBeNull();

        act(() => {
            result.current.setAccessToken("");
        });

        expect(result.current.auth).toBeFalsy();
        expect(result.current.accessToken).toEqual("");
    });

    test("setAccessToken should update store appropriately for token", () => {
        const { result } = renderHook(() => useAuthStore());
        act(() => {
            result.current.setAccessToken("testToken1");
        });

        expect(result.current.auth).toBeTruthy();
        expect(result.current.accessToken).toEqual("testToken1");
    });

    test("clearAccessToken should update store appropriately", () => {
        const { result } = renderHook(() => useAuthStore());
        act(() => {
            result.current.setAccessToken("testToken2");
        });

        expect(result.current.auth).toBeTruthy();
        expect(result.current.accessToken).toEqual("testToken2");

        act(() => {
            result.current.clearAccessToken();
        });

        expect(result.current.auth).toBeFalsy();
        expect(result.current.accessToken).toEqual("");
    });

    test("should persist in local storage", async () => {
        const { result, waitFor } = renderHook(() => useAuthStore());
        act(() => {
            result.current.setAccessToken("testToken3");
        });

        await waitFor(() => Boolean(result.current.accessToken));

        expect(localStorage.getItem("LOCAL_STORAGE_KEY_AUTH")).toEqual(
            `{"auth":true,"accessToken":"testToken3"}`
        );
    });
});
