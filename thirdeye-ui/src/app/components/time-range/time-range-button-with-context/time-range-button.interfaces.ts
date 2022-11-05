import { TimeRangeDuration } from "../time-range-provider/time-range-provider.interfaces";

export interface TimeRangeButtonProps {
    timeRangeDuration: TimeRangeDuration;
    recentCustomTimeRangeDurations?: TimeRangeDuration[];
    onChange?: (timeRangeDuration: TimeRangeDuration) => void;
    maxDate?: number;
    minDate?: number;
}

export interface TimeRangeButtonWithContextProps {
    btnGroupColor?: "secondary" | "inherit" | "primary" | "default";
    onTimeRangeChange?: (start: number, end: number) => void;
    maxDate?: number;
    minDate?: number;
}
