import i18n from "i18next";
import { kebabCase } from "lodash";
import { DateTime } from "luxon";
import {
    TimeRange,
    TimeRangeDuration,
} from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import { formatDateAndTime } from "../date-time/date-time.util";

export const createTimeRangeDuration = (
    timeRange: TimeRange,
    startTime: number,
    endTime: number
): TimeRangeDuration => {
    return {
        timeRange: timeRange,
        startTime: startTime,
        endTime: endTime,
    };
};

// Returns TimeRange.TODAY time range duration
export const getDefaultTimeRangeDuration = (): TimeRangeDuration => {
    return getTimeRangeDuration(TimeRange.TODAY);
};

export const getTimeRangeDuration = (
    timeRange: TimeRange
): TimeRangeDuration => {
    switch (timeRange) {
        case TimeRange.LAST_15_MINUTES: {
            const now = DateTime.local();
            const last15Minutes = now.minus({ minute: 15 });

            return createTimeRangeDuration(
                TimeRange.LAST_15_MINUTES,
                last15Minutes.toMillis(),
                now.toMillis()
            );
        }
        case TimeRange.LAST_1_HOUR: {
            const now = DateTime.local();
            const last1Hour = now.minus({ hour: 1 });

            return createTimeRangeDuration(
                TimeRange.LAST_1_HOUR,
                last1Hour.toMillis(),
                now.toMillis()
            );
        }
        case TimeRange.LAST_12_HOURS: {
            const now = DateTime.local();
            const last12Hour = now.minus({ hour: 12 });

            return createTimeRangeDuration(
                TimeRange.LAST_12_HOURS,
                last12Hour.toMillis(),
                now.toMillis()
            );
        }
        case TimeRange.LAST_24_HOURS: {
            const now = DateTime.local();
            const last24Hour = now.minus({ hour: 24 });

            return createTimeRangeDuration(
                TimeRange.LAST_24_HOURS,
                last24Hour.toMillis(),
                now.toMillis()
            );
        }
        case TimeRange.LAST_7_DAYS: {
            const now = DateTime.local();
            const last7Days = now.minus({ day: 7 });

            return createTimeRangeDuration(
                TimeRange.LAST_7_DAYS,
                last7Days.toMillis(),
                now.toMillis()
            );
        }
        case TimeRange.LAST_30_DAYS: {
            const now = DateTime.local();
            const last30Days = now.minus({ day: 30 });

            return createTimeRangeDuration(
                TimeRange.LAST_30_DAYS,
                last30Days.toMillis(),
                now.toMillis()
            );
        }
        case TimeRange.TODAY: {
            const now = DateTime.local();
            const today = now.startOf("day");

            return createTimeRangeDuration(
                TimeRange.TODAY,
                today.toMillis(),
                now.toMillis()
            );
        }
        case TimeRange.YESTERDAY: {
            const now = DateTime.local();
            const yesterday = now.minus({ day: 1 });

            return createTimeRangeDuration(
                TimeRange.YESTERDAY,
                yesterday.startOf("day").toMillis(),
                yesterday.endOf("day").toMillis()
            );
        }
        case TimeRange.THIS_WEEK: {
            const now = DateTime.local();
            const thisWeek = now.startOf("week");

            return createTimeRangeDuration(
                TimeRange.THIS_WEEK,
                thisWeek.toMillis(),
                now.toMillis()
            );
        }
        case TimeRange.LAST_WEEK: {
            const now = DateTime.local();
            const lastWeek = now.minus({ week: 1 });

            return createTimeRangeDuration(
                TimeRange.LAST_WEEK,
                lastWeek.startOf("week").toMillis(),
                lastWeek.endOf("week").toMillis()
            );
        }
        case TimeRange.THIS_MONTH: {
            const now = DateTime.local();
            const thisMonth = now.startOf("month");

            return createTimeRangeDuration(
                TimeRange.THIS_MONTH,
                thisMonth.toMillis(),
                now.toMillis()
            );
        }
        case TimeRange.LAST_MONTH: {
            const now = DateTime.local();
            const lastMonth = now.minus({ month: 1 });

            return createTimeRangeDuration(
                TimeRange.LAST_MONTH,
                lastMonth.startOf("month").toMillis(),
                lastMonth.endOf("month").toMillis()
            );
        }
        case TimeRange.THIS_YEAR: {
            const now = DateTime.local();
            const thisYear = now.startOf("year");

            return createTimeRangeDuration(
                TimeRange.THIS_YEAR,
                thisYear.toMillis(),
                now.toMillis()
            );
        }
        case TimeRange.LAST_YEAR: {
            const now = DateTime.local();
            const lastYear = now.minus({ year: 1 });

            return createTimeRangeDuration(
                TimeRange.LAST_YEAR,
                lastYear.startOf("year").toMillis(),
                lastYear.endOf("year").toMillis()
            );
        }
        default: {
            return getDefaultTimeRangeDuration();
        }
    }
};

// Returns formatted string representation of time range
export const formatTimeRange = (timeRange: TimeRange): string => {
    if (!timeRange) {
        return "";
    }

    return i18n.t(`label.${kebabCase(timeRange)}`);
};

// Returns formatted string representation of time range duration
export const formatTimeRangeDuration = (
    timeRangeDuration: TimeRangeDuration
): string => {
    if (!timeRangeDuration) {
        return "";
    }

    return i18n.t("label.start-time-end-time", {
        startTime: formatDateAndTime(timeRangeDuration.startTime),
        endTime: formatDateAndTime(timeRangeDuration.endTime),
    });
};
