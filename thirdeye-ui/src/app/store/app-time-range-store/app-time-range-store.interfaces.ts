import { TimeRangeDuration } from "../../components/time-range-selector/time-range-selector.interfaces";

export type AppTimeRangeStore = {
    appTimeRangeDuration: TimeRangeDuration;
    recentCustomTimeRangeDurations: TimeRangeDuration[];
    setAppTimeRangeDuration: (timeRangeDuration: TimeRangeDuration) => void;
    getAppTimeRangeDuration: () => TimeRangeDuration;
};
