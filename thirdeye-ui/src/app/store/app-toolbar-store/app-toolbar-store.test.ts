import { act, renderHook } from "@testing-library/react-hooks";
import { Fragment } from "react";
import { useAppToolbarStore } from "./app-toolbar-store";

describe("App Toolbar Store", () => {
    test("should initialize default values", () => {
        const { result } = renderHook(() => useAppToolbarStore());

        expect(result.current.appToolbar).toBeNull();
    });

    test("setAppToolbar should update store appropriately", () => {
        const { result } = renderHook(() => useAppToolbarStore());
        act(() => {
            result.current.setAppToolbar(Fragment);
        });

        expect(result.current.appToolbar).toEqual(Fragment);
    });

    test("removeAppToolbar should update store appropriately", () => {
        const { result } = renderHook(() => useAppToolbarStore());
        act(() => {
            result.current.setAppToolbar(Fragment);
        });

        expect(result.current.appToolbar).toEqual(Fragment);

        act(() => {
            result.current.removeAppToolbar();
        });

        expect(result.current.appToolbar).toBeNull();
    });
});
