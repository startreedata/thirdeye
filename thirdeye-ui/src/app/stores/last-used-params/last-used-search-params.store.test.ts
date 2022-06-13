import { act, renderHook } from "@testing-library/react-hooks";
import { useLastUsedSearchParams } from "./last-used-search-params.store";

describe("Last Used Search Params Store", () => {
    // Reset the state after every test
    afterEach(() => {
        const { result } = renderHook(() => useLastUsedSearchParams());
        act(() => result.current.reset());
    });

    it("should initialize default values", () => {
        const { result } = renderHook(() => useLastUsedSearchParams());

        expect(result.current.pathsToQueryStrings).toEqual({});
    });

    it("setLastUsedForPath should update search string for path", () => {
        const { result } = renderHook(() => useLastUsedSearchParams());

        expect(result.current.pathsToQueryStrings).toEqual({});

        act(() => {
            result.current.setLastUsedForPath("hello", "world");
            result.current.setLastUsedForPath("foo", "bar");
        });

        expect(result.current.pathsToQueryStrings).toEqual({
            hello: "world",
            foo: "bar",
        });

        act(() => {
            result.current.setLastUsedForPath("hello", "123");
        });

        expect(result.current.pathsToQueryStrings).toEqual({
            hello: "123",
            foo: "bar",
        });
    });

    it("setLastUsedForPath should not update if search string is empty", () => {
        const { result } = renderHook(() => useLastUsedSearchParams());

        expect(result.current.pathsToQueryStrings).toEqual({});

        act(() => {
            result.current.setLastUsedForPath("hello", "");
        });

        expect(result.current.pathsToQueryStrings).toEqual({});
    });

    it("getLastUsedForPath should return undefined if path does not exist", () => {
        const { result } = renderHook(() => useLastUsedSearchParams());

        expect(result.current.pathsToQueryStrings).toEqual({});
        expect(result.current.getLastUsedForPath("hello")).toBeUndefined();
    });

    it("getLastUsedForPath should return correct value if entry exists", () => {
        const { result } = renderHook(() => useLastUsedSearchParams());

        expect(result.current.pathsToQueryStrings).toEqual({});

        act(() => {
            result.current.setLastUsedForPath("hello", "world");
            result.current.setLastUsedForPath("foo", "bar");
        });

        expect(result.current.getLastUsedForPath("hello")).toEqual("world");
    });
});
