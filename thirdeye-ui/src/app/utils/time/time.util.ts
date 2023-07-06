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
import { en, isoDuration } from "@musement/iso-duration";
import { DateTime } from "luxon";

isoDuration.setLocales(
    {
        en,
    },
    {
        fallbackLocale: "en",
    }
);

export const SECOND_IN_MILLISECONDS = 1000;
export const MINUTE_IN_MILLISECONDS = SECOND_IN_MILLISECONDS * 60;
export const HOUR_IN_MILLISECONDS = MINUTE_IN_MILLISECONDS * 60;
export const DAY_IN_MILLISECONDS = HOUR_IN_MILLISECONDS * 24;
export const WEEK_IN_MILLISECONDS = DAY_IN_MILLISECONDS * 7;
export const MONTH_IN_MILLISECONDS = 2629800000;
export const YEAR_IN_MILLISECONDS = 31557600000;

export const OFFSET_TO_MILLISECONDS: { [key: string]: number } = {
    D: DAY_IN_MILLISECONDS,
    W: WEEK_IN_MILLISECONDS,
    M: MONTH_IN_MILLISECONDS,
    Y: YEAR_IN_MILLISECONDS,
};

export const timezoneStringShort = (timezone: string | undefined): string => {
    return DateTime.now().setZone(timezone).offsetNameShort;
};

export const iso8601ToMilliseconds = (isoStr: string): number => {
    const duration = isoDuration(isoStr).parse();

    let msSoFar = 0;

    msSoFar += duration.seconds * SECOND_IN_MILLISECONDS;
    msSoFar += duration.minutes * MINUTE_IN_MILLISECONDS;
    msSoFar += duration.hours * HOUR_IN_MILLISECONDS;
    msSoFar += duration.days * DAY_IN_MILLISECONDS;
    msSoFar += duration.weeks * WEEK_IN_MILLISECONDS;
    msSoFar += duration.months * MONTH_IN_MILLISECONDS;
    msSoFar += duration.years * YEAR_IN_MILLISECONDS;

    return msSoFar;
};

export const iso8601ToHuman = (isoStr: string): string => {
    return isoDuration(isoStr).humanize("en");
};
