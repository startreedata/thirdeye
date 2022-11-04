import { act, renderHook } from "@testing-library/react-hooks";
import axios from "axios";
import { ActionStatus } from "../actions.interfaces";
import { useGetMetrics } from "./metrics.actions";

jest.mock("axios");
const mockedAxios = axios as jest.Mocked<typeof axios>;

describe("Metrics Actions", () => {
    describe("useGetMetrics", () => {
        it("should return initial default values", () => {
            const { result } = renderHook(() => useGetMetrics());

            expect(result.current.metrics).toBeNull();
            expect(result.current.getMetrics).toBeDefined();
            expect(result.current.status).toEqual(ActionStatus.Initial);
            expect(result.current.errorMessages).toEqual([]);
        });

        it("should update data appropriately when making a successful REST call", async () => {
            mockedAxios.get.mockResolvedValueOnce({
                data: [{ id: 123 }],
            });
            const { result, waitFor } = renderHook(() => useGetMetrics());

            await act(async () => {
                const promise = result.current.getMetrics();

                // Wait for state update
                await waitFor(
                    () => result.current.status === ActionStatus.Initial
                );

                // When REST call is in progress
                expect(result.current.metrics).toBeNull();
                expect(result.current.getMetrics).toBeDefined();
                expect(result.current.status).toEqual(ActionStatus.Working);
                expect(result.current.errorMessages).toEqual([]);

                return promise.then(() => {
                    // When REST call is completed
                    expect(result.current.metrics).toEqual([{ id: 123 }]);
                    expect(result.current.getMetrics).toBeDefined();
                    expect(result.current.status).toEqual(ActionStatus.Done);
                    expect(result.current.errorMessages).toEqual([]);
                });
            });
        });
    });
});
