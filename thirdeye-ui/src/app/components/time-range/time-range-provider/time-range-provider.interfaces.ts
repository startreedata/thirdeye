import { ReactNode } from "react";

export interface TimeRangeProviderProps {
    children?: ReactNode;
}

export interface UseTimeRangeProps {
    // Current app time range duration
    timeRangeDuration: TimeRangeDuration;
    // An ordered list of last few used custom time range durations
    recentCustomTimeRangeDurations: TimeRangeDuration[];
    // Sets app time range duration
    setTimeRangeDuration: (timeRangeDuration: TimeRangeDuration) => void;
    // Refreshes app time range so that listeners pick up the latest app time range duration
    refreshTimeRange: () => void;
}

export interface TimeRangeDuration {
    timeRange: TimeRange;
    startTime: number;
    endTime: number;
}

export enum TimeRange {
    CUSTOM = "CUSTOM",
    LAST_15_MINUTES = "LAST_15_MINUTES",
    LAST_1_HOUR = "LAST_1_HOUR",
    LAST_12_HOURS = "LAST_12_HOURS",
    LAST_24_HOURS = "LAST_24_HOURS",
    LAST_7_DAYS = "LAST_7_DAYS",
    LAST_30_DAYS = "LAST_30_DAYS",
    TODAY = "TODAY",
    YESTERDAY = "YESTERDAY",
    THIS_WEEK = "THIS_WEEK",
    LAST_WEEK = "LAST_WEEK",
    THIS_MONTH = "THIS_MONTH",
    LAST_MONTH = "LAST_MONTH",
    THIS_YEAR = "THIS_YEAR",
    LAST_YEAR = "LAST_YEAR",
}
