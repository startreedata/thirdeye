/*
 * Copyright 2023 StarTree Inc
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

import { bisector, tickStep } from "d3-array";
import { DateTime } from "luxon";
import {
    DAY_IN_MILLISECONDS,
    HOUR_IN_MILLISECONDS,
    MINUTE_IN_MILLISECONDS,
    MONTH_IN_MILLISECONDS,
    SECOND_IN_MILLISECONDS,
    YEAR_IN_MILLISECONDS,
} from "../../../../../utils/time/time.util";
import {
    timeDay,
    timeHour,
    timeMillisecond,
    timeMinute,
    timeMonth,
    timeSecond,
    timeWeekday,
    timeYear,
} from "./time-intervals";
import { TimeInterval } from "./time-intervals.interfaces";

/**
 * Overrides and provides same functionality as https://github.com/d3/d3-scale/blob/main/src/time.js
 * but uses Luxon and inheritly supports timezones
 *
 * See specs for `toLocaleString` at
 * https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference
 * /Global_Objects/Intl/DateTimeFormat/DateTimeFormat
 *
 * @param {DateTime} dateLuxon - Date time object to convert into string for chart tick
 */
export const tickFormat = (dateLuxon: DateTime): string => {
    if (timeSecond(dateLuxon) < dateLuxon) {
        return dateLuxon.toLocaleString({ second: "2-digit" });
    } else if (timeMinute(dateLuxon) < dateLuxon) {
        return dateLuxon.toLocaleString({ second: "2-digit" });
    } else if (timeHour(dateLuxon) < dateLuxon) {
        return dateLuxon.toLocaleString({
            hour: "2-digit",
            minute: "2-digit",
            timeZoneName: "short",
        });
    } else if (timeDay(dateLuxon) < dateLuxon) {
        return dateLuxon.toLocaleString({
            hour: "2-digit",
            hour12: true,
            timeZoneName: "short",
        });
    } else if (timeMonth(dateLuxon) < dateLuxon) {
        if (timeWeekday(0)(dateLuxon) < dateLuxon) {
            return dateLuxon.toLocaleString({
                weekday: "short",
                day: "2-digit",
            });
        } else {
            return dateLuxon.toLocaleString({ month: "short", day: "2-digit" });
        }
    } else if (timeYear(dateLuxon) < dateLuxon) {
        return dateLuxon.toLocaleString({ month: "long" });
    } else {
        return dateLuxon.toLocaleString({ year: "numeric" });
    }
};

const TICK_INTERVALS: [TimeInterval, number, number][] = [
    [timeSecond, 1, SECOND_IN_MILLISECONDS],
    [timeSecond, 5, 5 * SECOND_IN_MILLISECONDS],
    [timeSecond, 15, 15 * SECOND_IN_MILLISECONDS],
    [timeSecond, 30, 30 * SECOND_IN_MILLISECONDS],
    [timeMinute, 1, MINUTE_IN_MILLISECONDS],
    [timeSecond, 5, 5 * MINUTE_IN_MILLISECONDS], // 5
    [timeSecond, 15, 15 * MINUTE_IN_MILLISECONDS],
    [timeSecond, 30, 30 * MINUTE_IN_MILLISECONDS],
    [timeHour, 1, HOUR_IN_MILLISECONDS],
    [timeHour, 3, 3 * HOUR_IN_MILLISECONDS], // 9
    [timeHour, 6, 6 * HOUR_IN_MILLISECONDS],
    [timeHour, 12, 12 * HOUR_IN_MILLISECONDS], // 11
    [timeDay, 1, DAY_IN_MILLISECONDS], // 12
    [timeDay, 2, 2 * DAY_IN_MILLISECONDS],
    // Sunday
    [timeWeekday(0), 1, DAY_IN_MILLISECONDS],
    [timeMonth, 1, MONTH_IN_MILLISECONDS], // 15
    [timeMonth, 3, 3 * MONTH_IN_MILLISECONDS],
    [timeYear, 1, YEAR_IN_MILLISECONDS],
];

const determineTickInterval = (
    start: DateTime,
    stop: DateTime,
    count: number
): TimeInterval => {
    const rangeDurationInMs = Math.abs(stop.toMillis() - start.toMillis());
    const evenSpacingBetweenTicks = rangeDurationInMs / count;
    const matchingIdx = bisector(([, , step]) => step).right(
        TICK_INTERVALS,
        evenSpacingBetweenTicks
    );

    if (matchingIdx === TICK_INTERVALS.length) {
        return timeYear.every(
            tickStep(
                start.toMillis() / YEAR_IN_MILLISECONDS,
                stop.toMillis() / YEAR_IN_MILLISECONDS,
                count
            )
        );
    }

    if (matchingIdx === 0) {
        return timeMillisecond.every(
            Math.max(tickStep(start.toMillis(), stop.toMillis(), count), 1)
        );
    }

    let [timeIntervalFunc, step] = TICK_INTERVALS[matchingIdx];

    if (
        evenSpacingBetweenTicks / TICK_INTERVALS[matchingIdx - 1][2] <
        TICK_INTERVALS[matchingIdx][2] / evenSpacingBetweenTicks
    ) {
        [timeIntervalFunc, step] = TICK_INTERVALS[matchingIdx - 1];
    }

    return timeIntervalFunc.every(step);
};

export const generateTicksForDateRange = (
    start: Date,
    stop: Date,
    count: number,
    timezone?: string
): Date[] => {
    let startLuxon = DateTime.fromJSDate(start);
    let stopLuxon = DateTime.fromJSDate(stop);

    if (timezone) {
        startLuxon = startLuxon.setZone(timezone);
        stopLuxon = stopLuxon.setZone(timezone);
    }

    const reverse = stopLuxon < startLuxon;

    if (reverse) {
        [startLuxon, stopLuxon] = [stopLuxon, startLuxon];
    }

    const interval = determineTickInterval(startLuxon, stopLuxon, count);

    const ticks = interval
        ? interval
              .range(startLuxon, stopLuxon)
              .map((d: DateTime) => d.toJSDate())
        : [];

    return reverse ? ticks.reverse() : ticks;
};
