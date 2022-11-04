import { TimeRangeDuration } from "../time-range-provider/time-range-provider.interfaces";

export interface TimeRangeButtonProps {
    timeRangeDuration: TimeRangeDuration;
    recentCustomTimeRangeDurations?: TimeRangeDuration[];
    onChange?: (timeRangeDuration: TimeRangeDuration) => void;
    maxDate?: number;
    minDate?: number;
    btnGroupColor?: "secondary" | "inherit" | "primary" | "default";
}
