import { TimeRangeDuration } from "../../time-range-provider/time-range-provider.interfaces";

export interface TimeRangeSelectorPopoverProps {
    recentCustomTimeRangeDurations?: TimeRangeDuration[];
    start: number;
    end: number;
    onChange?: (start: number, end: number) => void;
    timeRangeDuration?: TimeRangeDuration;
    onClose?: () => void;
    maxDate?: number;
    minDate?: number;
}
