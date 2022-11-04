import { TimeRangeDuration } from "../../time-range-provider/time-range-provider.interfaces";

export interface TimeRangeSelectorProps {
    hideRefresh?: boolean;
    hideTimeRange?: boolean;
    hideTimeRangeSelectorButton?: boolean;
    timeRangeDuration?: TimeRangeDuration;
    recentCustomTimeRangeDurations?: TimeRangeDuration[];
    onChange?: (timeRangeDuration: TimeRangeDuration) => void;
    onRefresh?: () => void;
}
