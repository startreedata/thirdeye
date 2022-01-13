import { act, renderHook } from "@testing-library/react-hooks";
import axios from "axios";
import { AnomalyBreakdownAPIOffsetValues } from "../../pages/anomalies-view-page/anomalies-view-page.interfaces";
import { ActionStatus } from "../actions.interfaces";
import { useGetAnomalyMetricBreakdown } from "./rca.actions";

const mockDataResponse = {
    id: 1,
};

jest.mock("axios");
const mockedAxios = axios as jest.Mocked<typeof axios>;

describe("RCA Actions", () => {
    describe("useGetAnomalyMetricBreakdown", () => {
        it("should return initial default values", () => {
            const { result } = renderHook(() => useGetAnomalyMetricBreakdown());

            expect(result.current.anomalyMetricBreakdown).toBeNull();
            expect(result.current.getMetricBreakdown).toBeDefined();
            expect(result.current.status).toEqual(ActionStatus.Initial);
            expect(result.current.errorMessage).toEqual("");
        });

        it("should update data appropriately when making a successful REST call", async () => {
            mockedAxios.get.mockResolvedValueOnce({ data: mockDataResponse });
            const { result, waitFor } = renderHook(() =>
                useGetAnomalyMetricBreakdown()
            );

            await act(async () => {
                const promise = result.current.getMetricBreakdown(123, {
                    offset: AnomalyBreakdownAPIOffsetValues.ONE_WEEK_AGO,
                });

                // Wait for state update
                await waitFor(
                    () => result.current.status === ActionStatus.Initial
                );

                // When REST call is in progress
                expect(result.current.anomalyMetricBreakdown).toBeNull();
                expect(result.current.getMetricBreakdown).toBeDefined();
                expect(result.current.status).toEqual(ActionStatus.Working);
                expect(result.current.errorMessage).toEqual("");

                return promise.then(() => {
                    // When REST call is completed
                    expect(result.current.anomalyMetricBreakdown).toEqual(
                        mockDataResponse
                    );
                    expect(result.current.getMetricBreakdown).toBeDefined();
                    expect(result.current.status).toEqual(ActionStatus.Done);
                    expect(result.current.errorMessage).toEqual("");
                });
            });
        });
    });
});
