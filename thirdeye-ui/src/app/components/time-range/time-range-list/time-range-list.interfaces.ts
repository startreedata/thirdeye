import {
    TimeRange,
    TimeRangeDuration,
} from "../time-range-provider/time-range-provider.interfaces";

export interface TimeRangeListProps {
    recentCustomTimeRangeDurations?: TimeRangeDuration[];
    selectedTimeRange?: TimeRange;
    onClick?: (eventObject: TimeRangeDuration | TimeRange) => void;
    maxDate?: number;
    minDate?: number;
}
