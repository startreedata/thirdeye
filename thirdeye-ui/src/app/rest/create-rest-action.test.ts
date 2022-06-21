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

import { act, renderHook } from "@testing-library/react-hooks";
import axios from "axios";
import { ActionStatus } from "./actions.interfaces";
import { useHTTPAction } from "./create-rest-action";

const mockError = {
    response: {
        data: {
            list: [{ code: "TEST_ERROR", msg: "testError" }],
        },
    },
};

describe("Create Rest Action (useHTTPAction)", () => {
    it("should return initial default values", () => {
        const { result } = renderHook(() => useHTTPAction(axios.get));

        expect(result.current.data).toBeNull();
        expect(result.current.makeRequest).toBeDefined();
        expect(result.current.status).toEqual(ActionStatus.Initial);
        expect(result.current.errorMessages).toEqual([]);
    });

    it("should invoke passed fetch function when calling makeRequest", async () => {
        const mockFetchFunction = jest.fn();
        const { result } = renderHook(() => useHTTPAction(mockFetchFunction));
        await act(async () => {
            await result.current.makeRequest(
                "/api/hello/world",
                "another",
                "param"
            );
        });

        expect(mockFetchFunction).toHaveBeenCalledWith(
            "/api/hello/world",
            "another",
            "param"
        );
    });

    it("should update data appropriately when making a successful REST call when invoking makeRequest", async () => {
        const mockFetchFunction = jest.fn();
        const mockResponse = {
            data: {
                id: 123,
            },
        };
        const { result, waitFor } = renderHook(() =>
            useHTTPAction(mockFetchFunction)
        );

        mockFetchFunction.mockResolvedValue(mockResponse);

        await act(async () => {
            const promise = result.current.makeRequest("/api/hello/world");

            // Wait for state update
            await waitFor(() => result.current.status === ActionStatus.Initial);

            // When REST call is in progress
            expect(result.current.data).toBeNull();
            expect(result.current.makeRequest).toBeDefined();
            expect(result.current.status).toEqual(ActionStatus.Working);
            expect(result.current.errorMessages).toEqual([]);

            return promise.then(() => {
                // When REST call is completed
                expect(result.current.data).toEqual(mockResponse);
                expect(result.current.makeRequest).toBeDefined();
                expect(result.current.status).toEqual(ActionStatus.Done);
                expect(result.current.errorMessages).toEqual([]);
            });
        });
    });

    it("should update data appropriately when making an unsuccessful REST call when invoking makeRequest", async () => {
        const mockFetchFunction = jest.fn();
        const { result, waitFor } = renderHook(() =>
            useHTTPAction(mockFetchFunction)
        );

        mockFetchFunction.mockRejectedValue(mockError);

        await act(async () => {
            const promise = result.current.makeRequest("/api/hello/world");

            // Wait for state update
            await waitFor(() => result.current.status === ActionStatus.Initial);

            // When REST call is in progress
            expect(result.current.data).toBeNull();
            expect(result.current.makeRequest).toBeDefined();
            expect(result.current.status).toEqual(ActionStatus.Working);
            expect(result.current.errorMessages).toEqual([]);

            return promise.then(() => {
                // When REST call is completed
                expect(result.current.data).toBeNull();
                expect(result.current.makeRequest).toBeDefined();
                expect(result.current.status).toEqual(ActionStatus.Error);
                expect(result.current.errorMessages).toEqual(["testError"]);
            });
        });
    });
});
