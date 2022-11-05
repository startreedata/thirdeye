/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { act, renderHook } from "@testing-library/react-hooks";
import { ActionStatus } from "../actions.interfaces";
import { useGetInfoV1 } from "./info.actions";

jest.mock("./info.rest", () => ({
    getInfoV1: (mockGetInfoV1 = jest.fn()),
}));

describe("Info Actions", () => {
    it("useGetInfoV1 should return initial default values", () => {
        const { result } = renderHook(() => useGetInfoV1());

        expect(result.current.infoV1).toBeNull();
        expect(result.current.getInfoV1).toBeDefined();
        expect(result.current.status).toEqual(ActionStatus.Initial);
        expect(result.current.errorMessage).toEqual("");
    });

    it("getInfoV1 should invoke appropriate REST call", async () => {
        const { result } = renderHook(() => useGetInfoV1());
        await act(async () => {
            await result.current.getInfoV1();
        });

        expect(mockGetInfoV1).toHaveBeenCalled();
    });

    it("useGetInfoV1 should update data appropriately when making a successful REST call", async () => {
        mockGetInfoV1.mockResolvedValue(mockInfoV1);
        const { result, waitFor } = renderHook(() => useGetInfoV1());
        await act(async () => {
            const promise = result.current.getInfoV1();

            // Wait for state update
            await waitFor(() => result.current.status === ActionStatus.Initial);

            // When REST call is in progress
            expect(result.current.infoV1).toBeNull();
            expect(result.current.getInfoV1).toBeDefined();
            expect(result.current.status).toEqual(ActionStatus.Working);
            expect(result.current.errorMessage).toEqual("");

            return promise.then(() => {
                // When REST call is completed
                expect(result.current.infoV1).toEqual(mockInfoV1);
                expect(result.current.getInfoV1).toBeDefined();
                expect(result.current.status).toEqual(ActionStatus.Done);
                expect(result.current.errorMessage).toEqual("");
            });
        });
    });

    it("useGetInfoV1 should update data appropriately when making an unsuccessful REST call", async () => {
        mockGetInfoV1.mockRejectedValue(mockError);
        const { result, waitFor } = renderHook(() => useGetInfoV1());
        await act(async () => {
            const promise = result.current.getInfoV1();

            // Wait for state update
            await waitFor(() => result.current.status === ActionStatus.Initial);

            // When REST call is in progress
            expect(result.current.infoV1).toBeNull();
            expect(result.current.getInfoV1).toBeDefined();
            expect(result.current.status).toEqual(ActionStatus.Working);
            expect(result.current.errorMessage).toEqual("");

            return promise.then(() => {
                // When REST call is completed
                expect(result.current.infoV1).toBeNull();
                expect(result.current.getInfoV1).toBeDefined();
                expect(result.current.status).toEqual(ActionStatus.Error);
                expect(result.current.errorMessage).toEqual("testError");
            });
        });
    });
});

let mockGetInfoV1: jest.Mock;

const mockInfoV1 = {
    oidcIssuerUrl: "testOidcIssuerUrl",
};

const mockError = {
    response: {
        data: {
            message: "testError",
        },
    },
};
