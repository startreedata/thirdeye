import { act, renderHook } from "@testing-library/react-hooks";
import {
    TimeRange,
    TimeRangeDuration,
} from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import { useTimeRangeStore } from "./time-range.store";

jest.mock("../../utils/time-range/time-range.util", () => ({
    getDefaultTimeRangeDuration: jest.fn().mockImplementation(
        () =>
            (mockTimeRangeDuration1 = {
                timeRange: TimeRange.TODAY,
                startTime: 1,
                endTime: 2,
            })
    ),
    getTimeRangeDuration: jest
        .fn()
        .mockImplementation(() => mockTimeRangeDuration2),
}));

describe("Time Range Store", () => {
    it("should initialize default values", () => {
        const { result } = renderHook(() => useTimeRangeStore());

        expect(result.current.timeRangeDuration).toEqual(
            mockTimeRangeDuration1
        );
        expect(result.current.recentCustomTimeRangeDurations).toEqual([]);
    });

    it("setTimeRangeDuration should not update store for invalid time range duration", () => {
        const { result } = renderHook(() => useTimeRangeStore());

        expect(result.current.timeRangeDuration).toEqual(
            mockTimeRangeDuration1
        );
        expect(result.current.recentCustomTimeRangeDurations).toEqual([]);

        act(() => {
            result.current.setTimeRangeDuration(
                null as unknown as TimeRangeDuration
            );
        });

        expect(result.current.timeRangeDuration).toEqual(
            mockTimeRangeDuration1
        );
        expect(result.current.recentCustomTimeRangeDurations).toEqual([]);
    });

    it("setTimeRangeDuration should update store appropriately for predefined time range duration", () => {
        const { result } = renderHook(() => useTimeRangeStore());
        act(() => {
            result.current.setTimeRangeDuration(mockTimeRangeDuration2);
        });

        expect(result.current.timeRangeDuration).toEqual(
            mockTimeRangeDuration2
        );
        expect(result.current.recentCustomTimeRangeDurations).toEqual([]);
    });

    it("setTimeRangeDuration should update store appropriately for custom time range duration", () => {
        const { result } = renderHook(() => useTimeRangeStore());
        act(() => {
            result.current.setTimeRangeDuration(mockTimeRangeDuration3);
            result.current.setTimeRangeDuration(mockTimeRangeDuration4);
            result.current.setTimeRangeDuration(mockTimeRangeDuration5);
            result.current.setTimeRangeDuration(mockTimeRangeDuration6);
        });

        expect(result.current.timeRangeDuration).toEqual(
            mockTimeRangeDuration6
        );
        expect(result.current.recentCustomTimeRangeDurations).toEqual([
            mockTimeRangeDuration4,
            mockTimeRangeDuration5,
            mockTimeRangeDuration6,
        ]);
    });

    it("refreshTimeRange should update store appropriately for predefined time range", () => {
        const { result } = renderHook(() => useTimeRangeStore());
        act(() => {
            result.current.setTimeRangeDuration(mockTimeRangeDuration1);
            result.current.refreshTimeRange();
        });

        expect(result.current.timeRangeDuration).toEqual(
            mockTimeRangeDuration2
        );
    });

    it("refreshTimeRange should update store appropriately for custom time range", () => {
        const { result } = renderHook(() => useTimeRangeStore());
        act(() => {
            result.current.setTimeRangeDuration(mockTimeRangeDuration3);
            result.current.refreshTimeRange();
        });

        expect(result.current.timeRangeDuration).toEqual(
            mockTimeRangeDuration3
        );
    });

    it("should persist in browser local storage", async () => {
        const { result, waitFor } = renderHook(() => useTimeRangeStore());
        act(() => {
            result.current.setTimeRangeDuration(mockTimeRangeDuration3);
            result.current.setTimeRangeDuration(mockTimeRangeDuration4);
            result.current.setTimeRangeDuration(mockTimeRangeDuration5);
        });
        await waitFor(() => Boolean(result.current.timeRangeDuration));

        expect(localStorage.getItem("LOCAL_STORAGE_KEY_TIME_RANGE")).toEqual(
            `{"` +
                `state":{` +
                `"timeRangeDuration":{"timeRange":"CUSTOM","startTime":9,"endTime":10},` +
                `"recentCustomTimeRangeDurations":[` +
                `{"timeRange":"CUSTOM","startTime":5,"endTime":6},` +
                `{"timeRange":"CUSTOM","startTime":7,"endTime":8},` +
                `{"timeRange":"CUSTOM","startTime":9,"endTime":10}` +
                `]` +
                `},` +
                `"version":0` +
                `}`
        );
    });
});

let mockTimeRangeDuration1: TimeRangeDuration;

const mockTimeRangeDuration2 = {
    timeRange: TimeRange.YESTERDAY,
    startTime: 3,
    endTime: 4,
};

const mockTimeRangeDuration3 = {
    timeRange: TimeRange.CUSTOM,
    startTime: 5,
    endTime: 6,
};

const mockTimeRangeDuration4 = {
    timeRange: TimeRange.CUSTOM,
    startTime: 7,
    endTime: 8,
};

const mockTimeRangeDuration5 = {
    timeRange: TimeRange.CUSTOM,
    startTime: 9,
    endTime: 10,
};

const mockTimeRangeDuration6 = {
    timeRange: TimeRange.CUSTOM,
    startTime: 11,
    endTime: 12,
};
