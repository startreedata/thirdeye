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
import axios from "axios";
import { ActionStatus } from "../actions.interfaces";
import { useGetAlert, useGetEvaluation } from "./alerts.actions";

const mockEvaluation = {
    id: 1,
};

const mockAlert = {
    alert: {
        id: 123,
    },
    start: 1637802840000,
    end: 1638591300000,
};

jest.mock("axios");
const mockedAxios = axios as jest.Mocked<typeof axios>;

describe("Alerts Actions", () => {
    describe("useGetEvaluation", () => {
        it("should return initial default values", () => {
            const { result } = renderHook(() => useGetEvaluation());

            expect(result.current.evaluation).toBeNull();
            expect(result.current.getEvaluation).toBeDefined();
            expect(result.current.status).toEqual(ActionStatus.Initial);
            expect(result.current.errorMessages).toEqual([]);
        });

        it("should update data appropriately when making a successful REST call", async () => {
            mockedAxios.post.mockResolvedValueOnce({ data: mockEvaluation });
            const { result, waitFor } = renderHook(() => useGetEvaluation());

            await act(async () => {
                const promise = result.current.getEvaluation(mockAlert);

                // Wait for state update
                await waitFor(
                    () => result.current.status === ActionStatus.Initial
                );

                // When REST call is in progress
                expect(result.current.evaluation).toBeNull();
                expect(result.current.getEvaluation).toBeDefined();
                expect(result.current.status).toEqual(ActionStatus.Working);
                expect(result.current.errorMessages).toEqual([]);

                return promise.then(() => {
                    // When REST call is completed
                    expect(result.current.evaluation).toEqual(mockEvaluation);
                    expect(result.current.getEvaluation).toBeDefined();
                    expect(result.current.status).toEqual(ActionStatus.Done);
                    expect(result.current.errorMessages).toEqual([]);
                });
            });
        });
    });

    describe("useGetAlert", () => {
        it("should return initial default values", () => {
            const { result } = renderHook(() => useGetAlert());

            expect(result.current.alert).toBeNull();
            expect(result.current.getAlert).toBeDefined();
            expect(result.current.status).toEqual(ActionStatus.Initial);
            expect(result.current.errorMessages).toEqual([]);
        });

        it("should update data appropriately when making a successful REST call", async () => {
            mockedAxios.get.mockResolvedValueOnce({
                data: { id: 123 },
            });
            const { result, waitFor } = renderHook(() => useGetAlert());

            await act(async () => {
                const promise = result.current.getAlert(123);

                // Wait for state update
                await waitFor(
                    () => result.current.status === ActionStatus.Initial
                );

                // When REST call is in progress
                expect(result.current.alert).toBeNull();
                expect(result.current.getAlert).toBeDefined();
                expect(result.current.status).toEqual(ActionStatus.Working);
                expect(result.current.errorMessages).toEqual([]);

                return promise.then(() => {
                    // When REST call is completed
                    expect(result.current.alert).toEqual({ id: 123 });
                    expect(result.current.getAlert).toBeDefined();
                    expect(result.current.status).toEqual(ActionStatus.Done);
                    expect(result.current.errorMessages).toEqual([]);
                });
            });
        });
    });
});
