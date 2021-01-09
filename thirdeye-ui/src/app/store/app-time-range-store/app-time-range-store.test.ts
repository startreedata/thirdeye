import { act, renderHook } from "@testing-library/react-hooks";
import {
    TimeRange,
    TimeRangeDuration,
} from "../../components/time-range-selector/time-range-selector.interfaces";
import { useAppTimeRangeStore } from "./app-time-range-store";

jest.mock("../../utils/time-range-util/time-range-util", () => ({
    getDefaultTimeRangeDuration: jest.fn().mockReturnValue(
        (mockTimeRangeDuration1 = {
            timeRange: TimeRange.TODAY,
            startTime: 1,
            endTime: 2,
        })
    ),
    getTimeRangeDuration: jest.fn().mockImplementation(() => {
        return mockTimeRangeDuration2;
    }),
}));

describe("App Time Range Store", () => {
    test("should initialize default values", () => {
        const { result } = renderHook(() => useAppTimeRangeStore());

        expect(result.current.appTimeRangeDuration).toEqual(
            mockTimeRangeDuration1
        );
        expect(result.current.recentCustomTimeRangeDurations).toEqual([]);
    });

    test("setAppTimeRangeDuration should not update store for invalid time range duration", () => {
        const { result } = renderHook(() => useAppTimeRangeStore());

        expect(result.current.appTimeRangeDuration).toEqual(
            mockTimeRangeDuration1
        );
        expect(result.current.recentCustomTimeRangeDurations).toEqual([]);

        act(() => {
            result.current.setAppTimeRangeDuration(
                (null as unknown) as TimeRangeDuration
            );
        });

        expect(result.current.appTimeRangeDuration).toEqual(
            mockTimeRangeDuration1
        );
        expect(result.current.recentCustomTimeRangeDurations).toEqual([]);
    });

    test("setAppTimeRangeDuration should update store appropriately for predefined time range duration", () => {
        const { result } = renderHook(() => useAppTimeRangeStore());
        act(() => {
            result.current.setAppTimeRangeDuration(mockTimeRangeDuration2);
        });

        expect(result.current.appTimeRangeDuration).toEqual(
            mockTimeRangeDuration2
        );
        expect(result.current.recentCustomTimeRangeDurations).toEqual([]);
    });

    test("setAppTimeRangeDuration should update store appropriately for custom time range duration", () => {
        const { result } = renderHook(() => useAppTimeRangeStore());
        act(() => {
            result.current.setAppTimeRangeDuration(mockTimeRangeDuration3);
            result.current.setAppTimeRangeDuration(mockTimeRangeDuration4);
            result.current.setAppTimeRangeDuration(mockTimeRangeDuration5);
            result.current.setAppTimeRangeDuration(mockTimeRangeDuration6);
        });

        expect(result.current.appTimeRangeDuration).toEqual(
            mockTimeRangeDuration6
        );
        expect(result.current.recentCustomTimeRangeDurations).toEqual([
            mockTimeRangeDuration4,
            mockTimeRangeDuration5,
            mockTimeRangeDuration6,
        ]);
    });

    test("refreshAppTimeRangeDuration should update store appropriately for predefined time range", () => {
        const { result } = renderHook(() => useAppTimeRangeStore());
        act(() => {
            result.current.setAppTimeRangeDuration(mockTimeRangeDuration1);
            result.current.refreshAppTimeRangeDuration();
        });

        expect(result.current.appTimeRangeDuration).toEqual(
            mockTimeRangeDuration2
        );
    });

    test("refreshAppTimeRangeDuration should update store appropriately for custom time range", () => {
        const { result } = renderHook(() => useAppTimeRangeStore());
        act(() => {
            result.current.setAppTimeRangeDuration(mockTimeRangeDuration3);
            result.current.refreshAppTimeRangeDuration();
        });

        expect(result.current.appTimeRangeDuration).toEqual(
            mockTimeRangeDuration3
        );
    });

    test("should persist in local storage", async () => {
        const { result, waitFor } = renderHook(() => useAppTimeRangeStore());
        act(() => {
            result.current.setAppTimeRangeDuration(mockTimeRangeDuration3);
            result.current.setAppTimeRangeDuration(mockTimeRangeDuration4);
            result.current.setAppTimeRangeDuration(mockTimeRangeDuration5);
        });

        await waitFor(() => Boolean(result.current.appTimeRangeDuration));

        expect(
            localStorage.getItem("LOCAL_STORAGE_KEY_APP_TIME_RANGE")
        ).toEqual(
            `{` +
                `"appTimeRangeDuration":{"timeRange":"CUSTOM","startTime":9,"endTime":10},` +
                `"recentCustomTimeRangeDurations":[` +
                `{"timeRange":"CUSTOM","startTime":5,"endTime":6},` +
                `{"timeRange":"CUSTOM","startTime":7,"endTime":8},` +
                `{"timeRange":"CUSTOM","startTime":9,"endTime":10}` +
                `]` +
                `}`
        );
    });
});

let mockTimeRangeDuration1: TimeRangeDuration;

const mockTimeRangeDuration2: TimeRangeDuration = {
    timeRange: TimeRange.YESTERDAY,
    startTime: 3,
    endTime: 4,
};

const mockTimeRangeDuration3: TimeRangeDuration = {
    timeRange: TimeRange.CUSTOM,
    startTime: 5,
    endTime: 6,
};

const mockTimeRangeDuration4: TimeRangeDuration = {
    timeRange: TimeRange.CUSTOM,
    startTime: 7,
    endTime: 8,
};

const mockTimeRangeDuration5: TimeRangeDuration = {
    timeRange: TimeRange.CUSTOM,
    startTime: 9,
    endTime: 10,
};

const mockTimeRangeDuration6: TimeRangeDuration = {
    timeRange: TimeRange.CUSTOM,
    startTime: 11,
    endTime: 12,
};
