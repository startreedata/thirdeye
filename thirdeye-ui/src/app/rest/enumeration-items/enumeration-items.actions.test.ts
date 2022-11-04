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
import axios from "axios";
import { ActionStatus } from "../actions.interfaces";
import {
    useGetEnumerationItem,
    useGetEnumerationItems,
} from "./enumeration-items.actions";

const mockEnumerationItem = {
    id: 1,
};

jest.mock("axios");
const mockedAxios = axios as jest.Mocked<typeof axios>;

describe("Enumeration Items Actions", () => {
    describe("useGetEnumerationItems", () => {
        it("should return initial default values", () => {
            const { result } = renderHook(() => useGetEnumerationItems());

            expect(result.current.enumerationItems).toBeNull();
            expect(result.current.getEnumerationItems).toBeDefined();
            expect(result.current.status).toEqual(ActionStatus.Initial);
            expect(result.current.errorMessages).toEqual([]);
        });

        it("should update data appropriately when making a successful REST call", async () => {
            mockedAxios.get.mockResolvedValueOnce({
                data: [mockEnumerationItem],
            });
            const { result, waitFor } = renderHook(() =>
                useGetEnumerationItems()
            );
            await act(async () => {
                const promise = result.current.getEnumerationItems({
                    ids: [1],
                });

                // Wait for state update
                await waitFor(
                    () => result.current.status === ActionStatus.Initial
                );

                // When REST call is in progress
                expect(result.current.enumerationItems).toBeNull();
                expect(result.current.getEnumerationItems).toBeDefined();
                expect(result.current.status).toEqual(ActionStatus.Working);
                expect(result.current.errorMessages).toEqual([]);

                return promise.then(() => {
                    // When REST call is completed
                    expect(result.current.enumerationItems).toEqual([
                        mockEnumerationItem,
                    ]);
                    expect(result.current.getEnumerationItems).toBeDefined();
                    expect(result.current.status).toEqual(ActionStatus.Done);
                    expect(result.current.errorMessages).toEqual([]);
                });
            });
        });
    });

    describe("useGetEnumerationItem", () => {
        it("should return initial default values", () => {
            const { result } = renderHook(() => useGetEnumerationItem());

            expect(result.current.enumerationItem).toBeNull();
            expect(result.current.getEnumerationItem).toBeDefined();
            expect(result.current.status).toEqual(ActionStatus.Initial);
            expect(result.current.errorMessages).toEqual([]);
        });

        it("should update data appropriately when making a successful REST call", async () => {
            mockedAxios.get.mockResolvedValueOnce({
                data: mockEnumerationItem,
            });
            const { result, waitFor } = renderHook(() =>
                useGetEnumerationItem()
            );
            await act(async () => {
                const promise = result.current.getEnumerationItem(1);

                // Wait for state update
                await waitFor(
                    () => result.current.status === ActionStatus.Initial
                );

                // When REST call is in progress
                expect(result.current.enumerationItem).toBeNull();
                expect(result.current.getEnumerationItem).toBeDefined();
                expect(result.current.status).toEqual(ActionStatus.Working);
                expect(result.current.errorMessages).toEqual([]);

                return promise.then(() => {
                    // When REST call is completed
                    expect(result.current.enumerationItem).toEqual(
                        mockEnumerationItem
                    );
                    expect(result.current.getEnumerationItem).toBeDefined();
                    expect(result.current.status).toEqual(ActionStatus.Done);
                    expect(result.current.errorMessages).toEqual([]);
                });
            });
        });
    });
});
