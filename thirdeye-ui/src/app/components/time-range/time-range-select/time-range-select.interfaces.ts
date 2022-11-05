import {
    TimeRange,
    TimeRangeDuration,
} from "../time-range-provider/time-range-provider.interfaces";

export interface TimeRangeSelectProps {
    recentCustomTimeRangeDurations?: TimeRangeDuration[];
    selectedTimeRange?: TimeRange;
    onChange?: (eventObject: TimeRangeDuration | TimeRange) => void;
    maxDate?: number;
    minDate?: number;
}
