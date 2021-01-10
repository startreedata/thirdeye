import {
    TimeRange,
    TimeRangeDuration,
} from "../time-range-selector.interfaces";

export interface TimeRangeListProps {
    recentCustomTimeRangeDurations?: TimeRangeDuration[];
    selectedTimeRange?: TimeRange;
    onClick?: (eventObject: TimeRangeDuration | TimeRange) => void;
}
