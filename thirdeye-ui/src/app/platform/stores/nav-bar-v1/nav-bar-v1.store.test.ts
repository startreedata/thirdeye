///
/// Copyright 2022 StarTree Inc
///
/// Licensed under the StarTree Community License (the "License"); you may not use
/// this file except in compliance with the License. You may obtain a copy of the
/// License at http://www.startree.ai/legal/startree-community-license
///
/// Unless required by applicable law or agreed to in writing, software distributed under the
/// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
/// either express or implied.
/// See the License for the specific language governing permissions and limitations under
/// the License.
///


// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { act, renderHook } from "@testing-library/react-hooks";
import { useNavBarV1 } from "./nav-bar-v1.store";

describe("Nav Bar V1", () => {
    it("should initialize default values", () => {
        const { result } = renderHook(() => useNavBarV1());

        expect(result.current.navBarMinimized).toBeFalsy();
        expect(result.current.navBarUserPreference).toEqual(0);
    });

    it("minimizeNavBar should update store appropriately", () => {
        const { result } = renderHook(() => useNavBarV1());
        act(() => {
            result.current.minimizeNavBar();
        });

        expect(result.current.navBarMinimized).toBeTruthy();
        expect(result.current.navBarUserPreference).toEqual(0);
    });

    it("maximizeNavBar should update store appropriately", () => {
        const { result } = renderHook(() => useNavBarV1());
        act(() => {
            result.current.maximizeNavBar();
        });

        expect(result.current.navBarMinimized).toBeFalsy();
        expect(result.current.navBarUserPreference).toEqual(0);
    });

    it("setNavBarUserPreference should update store appropriately for invalid user preference", () => {
        const { result } = renderHook(() => useNavBarV1());
        act(() => {
            result.current.setNavBarUserPreference(null as unknown as number);
        });

        expect(result.current.navBarMinimized).toBeFalsy();
        expect(result.current.navBarUserPreference).toEqual(0);
    });

    it("setNavBarUserPreference should update store appropriately for user preference", () => {
        const { result } = renderHook(() => useNavBarV1());
        act(() => {
            result.current.setNavBarUserPreference(1);
        });

        expect(result.current.navBarMinimized).toBeFalsy();
        expect(result.current.navBarUserPreference).toEqual(1);
    });

    it("should persist in browser local storage", async () => {
        expect(localStorage.getItem("nav-bar-v1")).toEqual(
            `{` +
                `"state":{` +
                `"navBarMinimized":false,` +
                `"navBarUserPreference":1` +
                `},` +
                `"version":0` +
                `}`
        );
    });
});
