import { TimeRangeDuration } from "../time-range-provider/time-range-provider.interfaces";

export interface TimeRangeButtonProps {
    timeRangeDuration: TimeRangeDuration;
    recentCustomTimeRangeDurations?: TimeRangeDuration[];
    onChange?: (timeRangeDuration: TimeRangeDuration) => void;
}

export interface TimeRangeButtonWithContextProps {
    onTimeRangeChange: (start: number, end: number) => void;
}
