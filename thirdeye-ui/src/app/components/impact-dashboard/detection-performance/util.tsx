/*
 * Copyright 2024 StarTree Inc
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
import { Anomaly } from "../../../rest/dto/anomaly.interfaces";

function dateToEpoch(date: Date): number {
    return Math.floor(date.getTime());
}

export function epochToDate(epoch: number): string {
    const date = new Date(epoch);

    return `${date.getMonth() + 1}/${date.getDate()}/${date.getFullYear()}`;
}

export function getTimeWindows(
    startTime: number,
    endTime: number,
    windowinHr = 24
): { windowStart: number; windowEnd: number }[] {
    const timeWindows = [];
    let currentStartTIme = new Date(startTime);
    const windowEndTime = new Date(endTime);
    while (currentStartTIme < windowEndTime) {
        const windowStart = new Date(currentStartTIme);
        const windowEnd = new Date(currentStartTIme);
        windowEnd.setHours(windowEnd.getHours() + windowinHr);

        timeWindows.push({
            windowStart: dateToEpoch(windowStart),
            windowEnd: dateToEpoch(windowEnd),
        });
        currentStartTIme = windowEnd;
    }

    return timeWindows;
}

export function groupDataByTimeWindows(
    data: Anomaly[],
    timeWindows: { windowStart: number; windowEnd: number }[]
): { windowStart: number; windowEnd: number; data: Anomaly[] }[] {
    return timeWindows.map(({ windowStart, windowEnd }) => {
        const windowData = data.filter(
            (entry) =>
                entry.endTime > windowStart && entry.startTime < windowEnd
        );

        return {
            windowStart,
            windowEnd,
            data: windowData,
        };
    });
}
