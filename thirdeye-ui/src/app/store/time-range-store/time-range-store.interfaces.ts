import { TimeRange } from "../../components/time-range/time-range-selector.interfaces";

export type TimeRangeStore = {
    timeRange: TimeRange;
    setTimeRange: (timeRange: TimeRange) => void;
};
