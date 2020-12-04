export enum TimeRange {
    LAST_15_MINUTES,
    LAST_1_HOUR,
    LAST_12_HOURS,
    LAST_24_HOURS,
    LAST_7_DAYS,
    LAST_30_DAYS,
    TODAY,
    YESTERDAY,
    THIS_WEEK,
    LAST_WEEK,
    THIS_MONTH,
    LAST_MONTH,
    THIS_YEAR,
    LAST_YEAR,
    CUSTOM,
}

export interface TimeRangeDuration {
    timeRange: TimeRange;
    startTime: number;
    endTime: number;
}

export interface TimeRangeSelectorProps {
    timeRange: TimeRange;
    getTimeRangeDuration: () => TimeRangeDuration;
    onChange: (
        timeRange: TimeRange,
        startTime?: number,
        endTime?: number
    ) => void;
}
