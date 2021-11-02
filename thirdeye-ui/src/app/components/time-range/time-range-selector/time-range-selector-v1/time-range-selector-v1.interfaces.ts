import { TimeRangeDuration } from "../../time-range-provider/time-range-provider.interfaces";

export interface TimeRangeSelectorProps {
    hideTimeRange?: boolean;
    hideTimeRangeSelectorButton?: boolean;
    timeRangeDuration?: TimeRangeDuration;
    recentCustomTimeRangeDurations?: TimeRangeDuration[];
    onChange?: (timeRangeDuration: TimeRangeDuration) => void;
    onRefresh?: () => void;
}
