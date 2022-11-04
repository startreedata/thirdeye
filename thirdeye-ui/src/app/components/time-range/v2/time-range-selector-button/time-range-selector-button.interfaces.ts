import { TimeRangeDuration } from "../../time-range-provider/time-range-provider.interfaces";

export interface TimeRangeSelectorButtonProps {
    start: number;
    end: number;
    recentCustomTimeRangeDurations?: TimeRangeDuration[];
    onChange?: (start: number, end: number) => void;
    maxDate?: number;
    minDate?: number;
    btnGroupColor?: "secondary" | "inherit" | "primary" | "default";
    fullWidth?: boolean;
    placeholder?: string;
}
