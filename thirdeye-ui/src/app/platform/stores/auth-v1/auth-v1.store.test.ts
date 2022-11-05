/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { act, renderHook } from "@testing-library/react-hooks";
import { useAuthV1 } from "./auth-v1.store";

describe("Auth V1", () => {
    it("should initialize default values", () => {
        const { result } = renderHook(() => useAuthV1());

        expect(result.current.authenticated).toBeFalsy();
        expect(result.current.authDisabled).toBeFalsy();
        expect(result.current.authDisabledNotification).toBeTruthy();
        expect(result.current.accessToken).toEqual("");
        expect(result.current.authExceptionCode).toEqual("");
        expect(result.current.redirectHref).toEqual("");
        expect(result.current.authAction).toEqual("");
        expect(result.current.authActionData).toEqual("");
    });

    it("enableAuth should update store appropriately", () => {
        const { result } = renderHook(() => useAuthV1());
        act(() => {
            result.current.enableAuth();
        });

        expect(result.current.authenticated).toBeFalsy();
        expect(result.current.authDisabled).toBeFalsy();
        expect(result.current.authDisabledNotification).toBeTruthy();
        expect(result.current.accessToken).toEqual("");
        expect(result.current.authExceptionCode).toEqual("");
        expect(result.current.redirectHref).toEqual("");
        expect(result.current.authAction).toEqual("");
        expect(result.current.authActionData).toEqual("");
    });

    it("disableAuth should update store appropriately", () => {
        const { result } = renderHook(() => useAuthV1());
        act(() => {
            result.current.disableAuth();
        });

        expect(result.current.authenticated).toBeFalsy();
        expect(result.current.authDisabled).toBeTruthy();
        expect(result.current.authDisabledNotification).toBeTruthy();
        expect(result.current.accessToken).toEqual("");
        expect(result.current.authExceptionCode).toEqual("");
        expect(result.current.redirectHref).toEqual("");
        expect(result.current.authAction).toEqual("");
        expect(result.current.authActionData).toEqual("");
    });

    it("clearAuthDisabledNotification should update store appropriately", () => {
        const { result } = renderHook(() => useAuthV1());
        act(() => {
            result.current.clearAuthDisabledNotification();
        });

        expect(result.current.authenticated).toBeFalsy();
        expect(result.current.authDisabled).toBeTruthy();
        expect(result.current.authDisabledNotification).toBeFalsy();
        expect(result.current.accessToken).toEqual("");
        expect(result.current.authExceptionCode).toEqual("");
        expect(result.current.redirectHref).toEqual("");
        expect(result.current.authAction).toEqual("");
        expect(result.current.authActionData).toEqual("");
    });

    it("setAccessToken should update store appropriately for invalid token", () => {
        const { result } = renderHook(() => useAuthV1());
        act(() => {
            result.current.setAccessToken(null as unknown as string);
        });

        expect(result.current.authenticated).toBeFalsy();
        expect(result.current.authDisabled).toBeFalsy();
        expect(result.current.authDisabledNotification).toBeTruthy();
        expect(result.current.accessToken).toEqual("");
        expect(result.current.authExceptionCode).toEqual("");
        expect(result.current.redirectHref).toEqual("");
        expect(result.current.authAction).toEqual("");
        expect(result.current.authActionData).toEqual("");
    });

    it("setAccessToken should update store appropriately for empty token", () => {
        const { result } = renderHook(() => useAuthV1());
        act(() => {
            result.current.setAccessToken("");
        });

        expect(result.current.authenticated).toBeFalsy();
        expect(result.current.authDisabled).toBeFalsy();
        expect(result.current.authDisabledNotification).toBeTruthy();
        expect(result.current.accessToken).toEqual("");
        expect(result.current.authExceptionCode).toEqual("");
        expect(result.current.redirectHref).toEqual("");
        expect(result.current.authAction).toEqual("");
        expect(result.current.authActionData).toEqual("");
    });

    it("setAccessToken should update store appropriately for token", () => {
        const { result } = renderHook(() => useAuthV1());
        act(() => {
            result.current.setAccessToken("testToken");
        });

        expect(result.current.authenticated).toBeTruthy();
        expect(result.current.authDisabled).toBeFalsy();
        expect(result.current.authDisabledNotification).toBeTruthy();
        expect(result.current.accessToken).toEqual("testToken");
        expect(result.current.authExceptionCode).toEqual("");
        expect(result.current.redirectHref).toEqual("");
        expect(result.current.authAction).toEqual("");
        expect(result.current.authActionData).toEqual("");
    });

    it("clearAuth should update store appropriately for invalid exception code", () => {
        const { result } = renderHook(() => useAuthV1());
        act(() => {
            result.current.clearAuth(null as unknown as string);
        });

        expect(result.current.authenticated).toBeFalsy();
        expect(result.current.authDisabled).toBeFalsy();
        expect(result.current.authDisabledNotification).toBeTruthy();
        expect(result.current.accessToken).toEqual("");
        expect(result.current.authExceptionCode).toEqual("");
        expect(result.current.redirectHref).toEqual("");
        expect(result.current.authAction).toEqual("");
        expect(result.current.authActionData).toEqual("");
    });

    it("clearAuth should update store appropriately for empty exception code", () => {
        const { result } = renderHook(() => useAuthV1());
        act(() => {
            result.current.clearAuth("");
        });

        expect(result.current.authenticated).toBeFalsy();
        expect(result.current.authDisabled).toBeFalsy();
        expect(result.current.authDisabledNotification).toBeTruthy();
        expect(result.current.accessToken).toEqual("");
        expect(result.current.authExceptionCode).toEqual("");
        expect(result.current.redirectHref).toEqual("");
        expect(result.current.authAction).toEqual("");
        expect(result.current.authActionData).toEqual("");
    });

    it("clearAuth should update store appropriately for exception code", () => {
        const { result } = renderHook(() => useAuthV1());
        act(() => {
            result.current.clearAuth("testExceptionCode");
        });

        expect(result.current.authenticated).toBeFalsy();
        expect(result.current.authDisabled).toBeFalsy();
        expect(result.current.authDisabledNotification).toBeTruthy();
        expect(result.current.accessToken).toEqual("");
        expect(result.current.authExceptionCode).toEqual("testExceptionCode");
        expect(result.current.redirectHref).toEqual("");
        expect(result.current.authAction).toEqual("");
        expect(result.current.authActionData).toEqual("");
    });

    it("clearAuthException should update store appropriately", () => {
        const { result } = renderHook(() => useAuthV1());
        act(() => {
            result.current.clearAuthException();
        });

        expect(result.current.authenticated).toBeFalsy();
        expect(result.current.authDisabled).toBeFalsy();
        expect(result.current.authDisabledNotification).toBeTruthy();
        expect(result.current.accessToken).toEqual("");
        expect(result.current.authExceptionCode).toEqual("");
        expect(result.current.redirectHref).toEqual("");
        expect(result.current.authAction).toEqual("");
        expect(result.current.authActionData).toEqual("");
    });

    it("setRedirectHref should update store appropriately for href", () => {
        const { result } = renderHook(() => useAuthV1());
        act(() => {
            result.current.setRedirectHref("/testHref");
        });

        expect(result.current.authenticated).toBeFalsy();
        expect(result.current.authDisabled).toBeFalsy();
        expect(result.current.authDisabledNotification).toBeTruthy();
        expect(result.current.accessToken).toEqual("");
        expect(result.current.authExceptionCode).toEqual("");
        expect(result.current.redirectHref).toEqual("/testHref");
        expect(result.current.authAction).toEqual("");
        expect(result.current.authActionData).toEqual("");
    });

    it("clearRedirectHref should update store appropriately", () => {
        const { result } = renderHook(() => useAuthV1());
        act(() => {
            result.current.clearRedirectHref();
        });

        expect(result.current.authenticated).toBeFalsy();
        expect(result.current.authDisabled).toBeFalsy();
        expect(result.current.authDisabledNotification).toBeTruthy();
        expect(result.current.accessToken).toEqual("");
        expect(result.current.authExceptionCode).toEqual("");
        expect(result.current.redirectHref).toEqual("");
        expect(result.current.authAction).toEqual("");
        expect(result.current.authActionData).toEqual("");
    });

    it("setAuthAction should update store appropriately for invalid action and action data", () => {
        const { result } = renderHook(() => useAuthV1());
        act(() => {
            result.current.setAuthAction(
                null as unknown as string,
                null as unknown as string
            );
        });

        expect(result.current.authenticated).toBeFalsy();
        expect(result.current.authDisabled).toBeFalsy();
        expect(result.current.authDisabledNotification).toBeTruthy();
        expect(result.current.accessToken).toEqual("");
        expect(result.current.authExceptionCode).toEqual("");
        expect(result.current.redirectHref).toEqual("");
        expect(result.current.authAction).toEqual("");
        expect(result.current.authActionData).toEqual("");
    });

    it("setAuthAction should update store appropriately for empty action and action data", () => {
        const { result } = renderHook(() => useAuthV1());
        act(() => {
            result.current.setAuthAction("", "");
        });

        expect(result.current.authenticated).toBeFalsy();
        expect(result.current.authDisabled).toBeFalsy();
        expect(result.current.authDisabledNotification).toBeTruthy();
        expect(result.current.accessToken).toEqual("");
        expect(result.current.authExceptionCode).toEqual("");
        expect(result.current.redirectHref).toEqual("");
        expect(result.current.authAction).toEqual("");
        expect(result.current.authActionData).toEqual("");
    });

    it("setAuthAction should update store appropriately for action and action data", () => {
        const { result } = renderHook(() => useAuthV1());
        act(() => {
            result.current.setAuthAction("testAction", "testActionData");
        });

        expect(result.current.authenticated).toBeFalsy();
        expect(result.current.authDisabled).toBeFalsy();
        expect(result.current.authDisabledNotification).toBeTruthy();
        expect(result.current.accessToken).toEqual("");
        expect(result.current.authExceptionCode).toEqual("");
        expect(result.current.redirectHref).toEqual("");
        expect(result.current.authAction).toEqual("testAction");
        expect(result.current.authActionData).toEqual("testActionData");
    });

    it("clearAuthAction should update store appropriately", () => {
        const { result } = renderHook(() => useAuthV1());
        act(() => {
            result.current.clearAuthAction();
        });

        expect(result.current.authenticated).toBeFalsy();
        expect(result.current.authDisabled).toBeFalsy();
        expect(result.current.authDisabledNotification).toBeTruthy();
        expect(result.current.accessToken).toEqual("");
        expect(result.current.authExceptionCode).toEqual("");
        expect(result.current.redirectHref).toEqual("");
        expect(result.current.authAction).toEqual("");
        expect(result.current.authActionData).toEqual("");
    });

    it("should persist in browser local storage", async () => {
        const { result, waitFor } = renderHook(() => useAuthV1());
        act(() => {
            result.current.setAccessToken("testToken");
            result.current.setRedirectHref("/testHref");
            result.current.setAuthAction("testAction", "testActionData");
        });

        // Wait for state update
        await waitFor(() => Boolean(result.current.accessToken));

        expect(localStorage.getItem("auth-v1")).toEqual(
            `{` +
                `"state":{` +
                `"authenticated":true,` +
                `"authDisabled":false,` +
                `"authDisabledNotification":true,` +
                `"accessToken":"testToken",` +
                `"authExceptionCode":"",` +
                `"redirectHref":"/testHref"` +
                `},` +
                `"version":0` +
                `}`
        );
    });
});
