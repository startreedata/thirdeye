import {
    TimeRange,
    TimeRangeDuration,
} from "../time-range-provider/time-range-provider.interfaces";

export interface TimeRangeSelectorPopoverProps {
    recentCustomTimeRangeDurations?: TimeRangeDuration[];
    selectedTimeRange?: TimeRange;
    onChange?: (eventObject: TimeRangeDuration) => void;

    timeRangeDuration?: TimeRangeDuration;

    onClose?: () => void;
}
