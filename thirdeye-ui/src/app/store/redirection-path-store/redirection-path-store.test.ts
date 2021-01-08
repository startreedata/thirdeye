import { act, renderHook } from "@testing-library/react-hooks";
import { useRedirectionPathStore } from "./redirection-path-store";

describe("Redirection Path Store", () => {
    test("should initialize default values", () => {
        const { result } = renderHook(() => useRedirectionPathStore());

        expect(result.current.redirectionPath).toEqual("");
    });

    test("setRedirectionPath should update store appropriately", () => {
        const { result } = renderHook(() => useRedirectionPathStore());
        act(() => {
            result.current.setRedirectionPath("testPath1");
        });

        expect(result.current.redirectionPath).toEqual("testPath1");
    });

    test("clearRedirectionPath should update store appropriately", () => {
        const { result } = renderHook(() => useRedirectionPathStore());
        act(() => {
            result.current.setRedirectionPath("testPath2");
        });

        expect(result.current.redirectionPath).toEqual("testPath2");

        act(() => {
            result.current.clearRedirectionPath();
        });

        expect(result.current.redirectionPath).toEqual("");
    });
});
