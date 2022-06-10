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
import { ActionStatus } from "../actions.interfaces";
import {
    useGetAlertTemplate,
    useGetAlertTemplates,
} from "./alert-templates.actions";

jest.mock("axios");
const mockedAxios = axios as jest.Mocked<typeof axios>;

describe("Alert Templates Actions", () => {
    describe("useGetAlertTemplate", () => {
        it("should return initial default values", () => {
            const { result } = renderHook(() => useGetAlertTemplate());

            expect(result.current.alertTemplate).toBeNull();
            expect(result.current.getAlertTemplate).toBeDefined();
            expect(result.current.status).toEqual(ActionStatus.Initial);
            expect(result.current.errorMessages).toEqual([]);
        });

        it("should update data appropriately when making a successful REST call", async () => {
            mockedAxios.get.mockResolvedValueOnce({
                data: { id: 123 },
            });
            const { result, waitFor } = renderHook(() => useGetAlertTemplate());

            await act(async () => {
                const promise = result.current.getAlertTemplate(123);

                // Wait for state update
                await waitFor(
                    () => result.current.status === ActionStatus.Initial
                );

                // When REST call is in progress
                expect(result.current.alertTemplate).toBeNull();
                expect(result.current.getAlertTemplate).toBeDefined();
                expect(result.current.status).toEqual(ActionStatus.Working);
                expect(result.current.errorMessages).toEqual([]);

                return promise.then(() => {
                    // When REST call is completed
                    expect(result.current.alertTemplate).toEqual({ id: 123 });
                    expect(result.current.getAlertTemplate).toBeDefined();
                    expect(result.current.status).toEqual(ActionStatus.Done);
                    expect(result.current.errorMessages).toEqual([]);
                });
            });
        });
    });

    describe("useGetAlertTemplates", () => {
        it("should return initial default values", () => {
            const { result } = renderHook(() => useGetAlertTemplates());

            expect(result.current.alertTemplates).toBeNull();
            expect(result.current.getAlertTemplates).toBeDefined();
            expect(result.current.status).toEqual(ActionStatus.Initial);
            expect(result.current.errorMessages).toEqual([]);
        });

        it("should update data appropriately when making a successful REST call", async () => {
            mockedAxios.get.mockResolvedValueOnce({
                data: { id: 123 },
            });
            const { result, waitFor } = renderHook(() =>
                useGetAlertTemplates()
            );

            await act(async () => {
                const promise = result.current.getAlertTemplates();

                // Wait for state update
                await waitFor(
                    () => result.current.status === ActionStatus.Initial
                );

                // When REST call is in progress
                expect(result.current.alertTemplates).toBeNull();
                expect(result.current.getAlertTemplates).toBeDefined();
                expect(result.current.status).toEqual(ActionStatus.Working);
                expect(result.current.errorMessages).toEqual([]);

                return promise.then(() => {
                    // When REST call is completed
                    expect(result.current.alertTemplates).toEqual({ id: 123 });
                    expect(result.current.getAlertTemplates).toBeDefined();
                    expect(result.current.status).toEqual(ActionStatus.Done);
                    expect(result.current.errorMessages).toEqual([]);
                });
            });
        });
    });
});
