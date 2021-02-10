import { TimeRangeDuration } from "../time-range-provider/time-range-provider.interfaces";

export interface TimeRangeSelectorProps {
    timeRangeDuration?: TimeRangeDuration;
    recentCustomTimeRangeDurations?: TimeRangeDuration[];
    onChange?: (timeRangeDuration: TimeRangeDuration) => void;
    onRefresh?: () => void;
}
