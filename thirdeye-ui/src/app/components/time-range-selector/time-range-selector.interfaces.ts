export interface TimeRangeSelectorProps {
    timeRangeDuration: TimeRangeDuration;
    recentCustomTimeRangeDurations?: TimeRangeDuration[];
    getTimeRangeDuration: () => TimeRangeDuration;
    onChange?: (timeRangeDuration: TimeRangeDuration) => void;
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
