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
