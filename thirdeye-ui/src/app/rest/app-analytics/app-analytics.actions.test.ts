import { act, renderHook } from "@testing-library/react-hooks";
import axios from "axios";
import { ActionStatus } from "../actions.interfaces";
import { useGetAppAnalytics } from "./app-analytics.action";

const mockAppAnalyticsResponse = {
    version: "1.87.0-b152c0e410dc4199239f57f15ad6d512516940b4",
    nMonitoredMetrics: 11,
    anomalyStats: {
        totalCount: 2355,
        countWithFeedback: 47,
        feedbackStats: {
            ANOMALY: 12,
            NOT_ANOMALY: 3,
            NO_FEEDBACK: 1,
            ANOMALY_EXPECTED: 29,
            ANOMALY_NEW_TREND: 2,
        },
    },
};

jest.mock("axios");
const mockedAxios = axios as jest.Mocked<typeof axios>;

describe("App Analytics Actions", () => {
    describe("useGetAppAnalytics", () => {
        it("should return initial default values", () => {
            const { result } = renderHook(() => useGetAppAnalytics());

            expect(result.current.appAnalytics).toBeNull();
            expect(result.current.getAppAnalytics).toBeDefined();
            expect(result.current.status).toEqual(ActionStatus.Initial);
            expect(result.current.errorMessages).toEqual([]);
        });

        it("should update data appropriately when making a successful REST call", async () => {
            mockedAxios.get.mockResolvedValueOnce({
                data: mockAppAnalyticsResponse,
            });
            const { result, waitFor } = renderHook(() => useGetAppAnalytics());

            await act(async () => {
                const promise = result.current.getAppAnalytics();

                // Wait for state update
                await waitFor(
                    () => result.current.status === ActionStatus.Initial
                );

                // When REST call is in progress
                expect(result.current.appAnalytics).toBeNull();
                expect(result.current.appAnalytics).toBeDefined();
                expect(result.current.status).toEqual(ActionStatus.Working);
                expect(result.current.errorMessages).toEqual([]);

                return promise.then(() => {
                    // When REST call is completed
                    expect(result.current.appAnalytics).toEqual(
                        mockAppAnalyticsResponse
                    );
                    expect(result.current.getAppAnalytics).toBeDefined();
                    expect(result.current.status).toEqual(ActionStatus.Done);
                    expect(result.current.errorMessages).toEqual([]);
                });
            });
        });
    });
});
