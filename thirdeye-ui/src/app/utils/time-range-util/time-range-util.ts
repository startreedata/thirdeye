import { DateTime } from "luxon";
import {
    TimeRange,
    TimeRangeType,
} from "../../components/time-range/time-range-selector.interfaces";

export const createTimeRange = (
    type: TimeRangeType,
    startTime: number,
    endTime: number
): TimeRange => {
    return {
        type: type,
        startTime: startTime,
        endTime: endTime,
    };
};

export const getTimeRange = (type: TimeRangeType): TimeRange => {
    switch (type) {
        case TimeRangeType.LAST_15_MINUTES: {
            const now = DateTime.local();
            const last15Minutes = now.minus({ minute: 15 });

            return createTimeRange(
                TimeRangeType.LAST_15_MINUTES,
                last15Minutes.toMillis(),
                now.toMillis()
            );
        }
        case TimeRangeType.LAST_1_HOUR: {
            const now = DateTime.local();
            const last1Hour = now.minus({ hour: 1 });

            return createTimeRange(
                TimeRangeType.LAST_1_HOUR,
                last1Hour.toMillis(),
                now.toMillis()
            );
        }
        case TimeRangeType.LAST_12_HOURS: {
            const now = DateTime.local();
            const last12Hour = now.minus({ hour: 12 });

            return createTimeRange(
                TimeRangeType.LAST_12_HOURS,
                last12Hour.toMillis(),
                now.toMillis()
            );
        }
        case TimeRangeType.LAST_24_HOURS: {
            const now = DateTime.local();
            const last24Hour = now.minus({ hour: 24 });

            return createTimeRange(
                TimeRangeType.LAST_24_HOURS,
                last24Hour.toMillis(),
                now.toMillis()
            );
        }
        case TimeRangeType.LAST_7_DAYS: {
            const now = DateTime.local();
            const last7Days = now.minus({ day: 7 });

            return createTimeRange(
                TimeRangeType.LAST_7_DAYS,
                last7Days.toMillis(),
                now.toMillis()
            );
        }
        case TimeRangeType.LAST_30_DAYS: {
            const now = DateTime.local();
            const last30Days = now.minus({ day: 30 });

            return createTimeRange(
                TimeRangeType.LAST_30_DAYS,
                last30Days.toMillis(),
                now.toMillis()
            );
        }
        case TimeRangeType.TODAY: {
            const now = DateTime.local();
            const today = now.startOf("day");

            return createTimeRange(
                TimeRangeType.TODAY,
                today.toMillis(),
                now.toMillis()
            );
        }
        case TimeRangeType.YESTERDAY: {
            const now = DateTime.local();
            const yesterday = now.minus({ day: 1 });

            return createTimeRange(
                TimeRangeType.YESTERDAY,
                yesterday.startOf("day").toMillis(),
                yesterday.endOf("day").toMillis()
            );
        }
        case TimeRangeType.THIS_WEEK: {
            const now = DateTime.local();
            const thisWeek = now.startOf("week");

            return createTimeRange(
                TimeRangeType.THIS_WEEK,
                thisWeek.toMillis(),
                now.toMillis()
            );
        }
        case TimeRangeType.LAST_WEEK: {
            const now = DateTime.local();
            const lastWeek = now.minus({ week: 1 });

            return createTimeRange(
                TimeRangeType.LAST_WEEK,
                lastWeek.startOf("week").toMillis(),
                lastWeek.endOf("week").toMillis()
            );
        }
        case TimeRangeType.THIS_MONTH: {
            const now = DateTime.local();
            const thisMonth = now.startOf("month");

            return createTimeRange(
                TimeRangeType.THIS_MONTH,
                thisMonth.toMillis(),
                now.toMillis()
            );
        }
        case TimeRangeType.LAST_MONTH: {
            const now = DateTime.local();
            const lastMonth = now.minus({ month: 1 });

            return createTimeRange(
                TimeRangeType.LAST_MONTH,
                lastMonth.startOf("month").toMillis(),
                lastMonth.endOf("month").toMillis()
            );
        }

        case TimeRangeType.THIS_YEAR: {
            const now = DateTime.local();
            const thisYear = now.startOf("year");

            return createTimeRange(
                TimeRangeType.THIS_YEAR,
                thisYear.toMillis(),
                now.toMillis()
            );
        }
        case TimeRangeType.LAST_YEAR: {
            const now = DateTime.local();
            const lastYear = now.minus({ year: 1 });

            return createTimeRange(
                TimeRangeType.LAST_YEAR,
                lastYear.startOf("year").toMillis(),
                lastYear.endOf("year").toMillis()
            );
        }
        default: {
            // Default to TimeRange.TODAY
            const now = DateTime.local();
            const today = now.startOf("day");

            return createTimeRange(
                TimeRangeType.TODAY,
                today.toMillis(),
                now.toMillis()
            );
        }
    }
};
