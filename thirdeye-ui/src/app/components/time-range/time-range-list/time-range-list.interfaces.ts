import {
    TimeRange,
    TimeRangeDuration,
} from "../time-range-provider/time-range-provider.interfaces";

export interface TimeRangeListProps {
    recentCustomTimeRangeDurations?: TimeRangeDuration[];
    selectedTimeRange?: TimeRange;
    onClick?: (event: TimeRangeDuration | TimeRange) => void;
}
