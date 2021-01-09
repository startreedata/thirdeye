import { TimeRangeDuration } from "../../components/time-range-selector/time-range-selector.interfaces";

export type AppTimeRangeStore = {
    // Current app time range duration
    appTimeRangeDuration: TimeRangeDuration;
    // An ordered list of last few used custom time range durations
    recentCustomTimeRangeDurations: TimeRangeDuration[];
    // Sets app time range duration
    setAppTimeRangeDuration: (timeRangeDuration: TimeRangeDuration) => void;
    // Refreshes app time range duration so that listeners pick up the latest app time range
    // duration
    refreshAppTimeRangeDuration: () => void;
};
