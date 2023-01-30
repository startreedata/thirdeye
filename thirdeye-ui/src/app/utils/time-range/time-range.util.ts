/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import i18n from "i18next";
import { kebabCase } from "lodash";
import { DateTime } from "luxon";
import {
    TimeRange,
    TimeRangeDuration,
} from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import { formatDateAndTimeV1 } from "../../platform/utils";

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
    timeRangeDuration: TimeRangeDuration,
    timezone?: string
): string => {
    if (!timeRangeDuration) {
        return "";
    }

    return formatStartAndEndDuration(
        timeRangeDuration.startTime,
        timeRangeDuration.endTime,
        timezone
    );
};

/**
 * @param startTime - Expected to be in milliseconds
 * @param endTime - Expected to be in milliseconds
 * @param timezone - Format the time in this timezone
 */
export const formatStartAndEndDuration = (
    startTime: number,
    endTime: number,
    timezone?: string
): string => {
    return i18n.t("label.start-time-end-time", {
        startTime: formatDateAndTimeV1(startTime, timezone),
        endTime: formatDateAndTimeV1(endTime, timezone),
    });
};
