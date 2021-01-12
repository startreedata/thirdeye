import { TimeRangeDuration } from "../../components/time-range/time-range-provider/time-range-provider.interfaces";

export type TimeRangeStore = {
    timeRangeDuration: TimeRangeDuration;
    recentCustomTimeRangeDurations: TimeRangeDuration[];
    setTimeRangeDuration: (timeRangeDuration: TimeRangeDuration) => void;
    refreshTimeRange: () => void;
};
