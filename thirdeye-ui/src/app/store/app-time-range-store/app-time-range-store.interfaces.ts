import {
    TimeRange,
    TimeRangeDuration,
} from "../../components/time-range-selector/time-range-selector.interfaces";

export type AppTimeRangeStore = {
    appTimeRange: TimeRange;
    startTime: number;
    endTime: number;
    setAppTimeRange: (
        timeRange: TimeRange,
        startTime?: number,
        endTime?: number
    ) => void;
    getAppTimeRangeDuration: () => TimeRangeDuration;
};
