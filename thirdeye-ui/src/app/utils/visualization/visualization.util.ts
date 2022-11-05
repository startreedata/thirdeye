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
import { ScaleTime } from "d3-scale";
import { isEmpty, isNil } from "lodash";
import { Interval } from "luxon";
import {
    formatDateV1,
    formatLargeNumberV1,
    formatMonthOfYearV1,
    formatTimeV1,
    formatYearV1,
} from "../../platform/utils";
import { DAY_IN_MILLISECONDS } from "../time/time.util";

export const SEPARATOR_DATE_TIME = "@";
export const NUM_TICKS = 8;

// Returns abbreviated string representation of number
// Equivalent to Number Util formatLargeNumber, but as required by D3
export const formatLargeNumberForVisualization = (
    num: number | { valueOf(): number }
): string => {
    if (isNil(num)) {
        return "";
    }

    let targetNum;
    if (typeof num === "number") {
        targetNum = num;
    } else if (num.valueOf && typeof num.valueOf() === "number") {
        targetNum = num.valueOf();
    }

    if (isNil(targetNum)) {
        return "";
    }

    return formatLargeNumberV1(targetNum);
};

// Returns formatted string representation of date based on scale domain interval
// For example:
// Interval > 2 years -> YYYY
// 2 years >= interval > 2 months -> MMM YYYY
// 2 months >= interval > 2 days -> MMM DD, YYYY
// 2 days >= interval -> MMM DD, YY SEPARATOR_DATE_TIME HH:MM AM/PM
export const formatDateTimeForTimeAxis = (
    date: number | { valueOf(): number },
    scale: ScaleTime<number, number>
): string => {
    if (isNil(date) || !scale) {
        return "";
    }

    let targetDate;
    if (typeof date === "number") {
        targetDate = date;
    } else if (date.valueOf && typeof date.valueOf() === "number") {
        targetDate = date.valueOf();
    }

    if (isNil(targetDate)) {
        return "";
    }

    // Determine scale domain interval duration
    const duration = Interval.fromDateTimes(
        scale.domain()[0],
        scale.domain()[1]
    ).toDuration();

    if (duration.as("years") > 2) {
        // YYYY
        return formatYearV1(targetDate);
    }

    if (duration.as("months") > 2) {
        // MMM YYYY
        return formatMonthOfYearV1(targetDate);
    }

    if (duration.as("days") > 2) {
        // MMM DD, YYYY
        return formatDateV1(targetDate);
    }

    // MMM DD, YYYY SEPARATOR_DATE_TIME HH:MM AM/PM
    return (
        formatDateV1(targetDate) +
        SEPARATOR_DATE_TIME +
        formatTimeV1(targetDate)
    );
};

// Returns equally spaced tick values for scale domain interval
export const getTickValuesForTimeAxis = (
    scale: ScaleTime<number, number>,
    numTicks?: number
): number[] => {
    if (!scale) {
        return [];
    }

    if (isNil(numTicks)) {
        numTicks = NUM_TICKS;
    }

    if (numTicks < 3) {
        // Just the scale domain start and end time
        return [scale.domain()[0].getTime(), scale.domain()[1].getTime()];
    }

    // Determine scale domain interval
    const interval = Interval.fromDateTimes(
        scale.domain()[0],
        scale.domain()[1]
    );
    const splitIntervals = interval.divideEqually(numTicks - 1); // Account for scale domain start and end time as two tick values
    if (isEmpty(splitIntervals)) {
        // Just the scale domain start and end time
        return [scale.domain()[0].getTime(), scale.domain()[1].getTime()];
    }

    const timeTickValues = [];
    for (let index = 0; index < splitIntervals.length - 1; index++) {
        // Ignore last interval, same as scale domain end time
        timeTickValues.push(splitIntervals[index].end.toMillis());
    }

    return [
        scale.domain()[0].getTime(),
        ...timeTickValues,
        scale.domain()[1].getTime(),
    ];
};

export const determineGranularity = (timestamps: number[]): number => {
    const timestampArrayLength = timestamps.length;
    let granularityBestGuess = DAY_IN_MILLISECONDS;
    let idx = 1;
    let timesSame = 0;

    /**
     * If the difference between timestamps is the same at least 3 times,
     * assume that is the granularity
     */
    while (idx < timestampArrayLength && timesSame < 3) {
        const diff = timestamps[idx] - timestamps[idx - 1];

        if (granularityBestGuess === diff) {
            timesSame++;
        }

        granularityBestGuess = diff;
        idx++;
    }

    return granularityBestGuess;
};

const OTHER = "other";

export const checkIfOtherDimension = (id: string | undefined): boolean => {
    if (!id) {
        return false;
    }

    return id.toLowerCase() === OTHER;
};
