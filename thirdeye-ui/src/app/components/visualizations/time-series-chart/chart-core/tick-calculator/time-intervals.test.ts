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

import { DateTime } from "luxon";
import { timeDay, timeHour, timeMinute, timeMonth } from "./time-intervals";

describe("Time Intervals", () => {
    describe("timeMinute", () => {
        it("should return top of the minute for floor", () => {
            const testInput = DateTime.fromObject(
                {
                    year: 2023,
                    month: 1,
                    day: 1,
                    hour: 6,
                    minute: 30,
                    second: 25,
                    millisecond: 25,
                },
                { zone: "UTC" }
            );

            expect(timeMinute.floor(testInput).millisecond).toEqual(0);
            expect(timeMinute.floor(testInput).second).toEqual(0);
            expect(timeMinute.floor(testInput).minute).toEqual(30);
            expect(timeMinute.floor(testInput).hour).toEqual(6);
            expect(timeMinute.floor(testInput).day).toEqual(1);
        });

        it("should return top of the next minute for ceil", () => {
            const testInput = DateTime.fromObject(
                {
                    year: 2023,
                    month: 1,
                    day: 1,
                    hour: 6,
                    minute: 30,
                    second: 25,
                    millisecond: 25,
                },
                { zone: "UTC" }
            );

            expect(timeMinute.ceil(testInput).millisecond).toEqual(0);
            expect(timeMinute.ceil(testInput).second).toEqual(0);
            expect(timeMinute.ceil(testInput).minute).toEqual(31);
        });

        it("should return expected changed datetime object using offset", () => {
            const testInput = DateTime.fromObject(
                {
                    year: 2023,
                    month: 1,
                    day: 1,
                    hour: 6,
                    minute: 30,
                    second: 25,
                    millisecond: 25,
                },
                { zone: "UTC" }
            );

            expect(timeMinute.offset(testInput, 1).hour).toEqual(6);
            expect(timeMinute.offset(testInput, 1).minute).toEqual(31);
        });

        it("should return expected changed datetime object using range", () => {
            const testStart = DateTime.fromObject(
                {
                    year: 2023,
                    month: 1,
                    day: 1,
                    hour: 6,
                    minute: 30,
                },
                { zone: "UTC" }
            );
            const testEnd = DateTime.fromObject(
                {
                    year: 2023,
                    month: 1,
                    day: 1,
                    hour: 6,
                    minute: 40,
                },
                { zone: "UTC" }
            );

            const result = timeMinute.range(testStart, testEnd);

            expect(result).toHaveLength(10);
            expect(result[0].second).toEqual(0);

            expect(result[0].minute).toEqual(30);
            expect(result[9].minute).toEqual(39);
        });
    });

    describe("timeHour", () => {
        it("should return top of the hour for floor", () => {
            const testInput = DateTime.fromObject(
                {
                    year: 2023,
                    month: 1,
                    day: 1,
                    hour: 6,
                    minute: 30,
                    second: 0,
                },
                { zone: "UTC" }
            );

            expect(timeHour.floor(testInput).minute).toEqual(0);
            expect(timeHour.floor(testInput).second).toEqual(0);
            expect(timeHour.floor(testInput).hour).toEqual(6);
            expect(timeHour.floor(testInput).day).toEqual(1);
        });

        it("should return top of the next hour for ceil", () => {
            const testInput = DateTime.fromObject(
                {
                    year: 2023,
                    month: 1,
                    day: 1,
                    hour: 6,
                },
                { zone: "UTC" }
            );

            expect(timeHour.ceil(testInput).hour).toEqual(6);
            expect(timeHour.ceil(testInput).minute).toEqual(0);
        });

        it("should return expected changed datetime object using offset", () => {
            const testInput = DateTime.fromObject(
                {
                    year: 2023,
                    month: 1,
                    day: 1,
                    hour: 6,
                },
                { zone: "UTC" }
            );

            expect(timeHour.offset(testInput, 1).hour).toEqual(7);
            expect(timeHour.offset(testInput, 1).minute).toEqual(0);

            expect(timeHour.offset(testInput, 24).hour).toEqual(6);
            expect(timeHour.offset(testInput, 24).day).toEqual(2);
            expect(timeHour.offset(testInput, 24).minute).toEqual(0);
        });

        it("should return expected changed datetime object using range", () => {
            const testStart = DateTime.fromObject(
                {
                    year: 2023,
                    month: 1,
                    day: 1,
                    hour: 6,
                },
                { zone: "UTC" }
            );
            const testEnd = DateTime.fromObject(
                {
                    year: 2023,
                    month: 1,
                    day: 1,
                    hour: 18,
                },
                { zone: "UTC" }
            );

            const result = timeHour.range(testStart, testEnd);

            expect(result).toHaveLength(12);
            expect(result[0].second).toEqual(0);
            expect(result[0].minute).toEqual(0);

            expect(result[0].hour).toEqual(6);
            expect(result[11].hour).toEqual(17);
        });
    });

    describe("timeDay", () => {
        it("should return top of the day for floor", () => {
            const testInput = DateTime.fromObject(
                {
                    year: 2023,
                    month: 1,
                    day: 1,
                    hour: 6,
                    minute: 30,
                    second: 0,
                },
                { zone: "UTC" }
            );

            expect(timeDay.floor(testInput).minute).toEqual(0);
            expect(timeDay.floor(testInput).second).toEqual(0);
            expect(timeDay.floor(testInput).hour).toEqual(0);
            expect(timeDay.floor(testInput).day).toEqual(1);
        });

        it("should return top of the next day for ceil", () => {
            const testInput = DateTime.fromObject(
                {
                    year: 2023,
                    month: 1,
                    day: 1,
                    hour: 6,
                },
                { zone: "UTC" }
            );

            expect(timeDay.ceil(testInput).hour).toEqual(0);
            expect(timeDay.ceil(testInput).minute).toEqual(0);
            expect(timeDay.ceil(testInput).day).toEqual(2);
        });

        it("should return expected changed datetime object using offset", () => {
            const testInput = DateTime.fromObject(
                {
                    year: 2023,
                    month: 1,
                    day: 1,
                    hour: 6,
                },
                { zone: "UTC" }
            );

            expect(timeDay.offset(testInput, 1).hour).toEqual(6);
            expect(timeDay.offset(testInput, 1).day).toEqual(2);

            expect(timeDay.offset(testInput, 24).hour).toEqual(6);
            expect(timeDay.offset(testInput, 24).day).toEqual(25);
        });

        it("should return expected changed datetime object using range", () => {
            const testStart = DateTime.fromObject(
                {
                    year: 2023,
                    month: 1,
                    day: 1,
                    hour: 6,
                },
                { zone: "UTC" }
            );
            const testEnd = DateTime.fromObject(
                {
                    year: 2023,
                    month: 1,
                    day: 6,
                    hour: 18,
                },
                { zone: "UTC" }
            );

            const result = timeDay.range(testStart, testEnd);

            expect(result).toHaveLength(5);
            expect(result[0].second).toEqual(0);
            expect(result[0].minute).toEqual(0);
            expect(result[0].hour).toEqual(0);

            // Because the start date is not top of day
            expect(result[0].day).toEqual(2);
            expect(result[4].day).toEqual(6);
        });
    });

    describe("timeMonth", () => {
        it("should return top of the month for floor", () => {
            const testInput = DateTime.fromObject(
                {
                    year: 2023,
                    month: 1,
                    day: 10,
                    hour: 6,
                    minute: 30,
                    second: 0,
                },
                { zone: "UTC" }
            );

            expect(timeMonth.floor(testInput).minute).toEqual(0);
            expect(timeMonth.floor(testInput).second).toEqual(0);
            expect(timeMonth.floor(testInput).hour).toEqual(0);
            expect(timeMonth.floor(testInput).day).toEqual(1);
        });

        it("should return top of the next month for ceil", () => {
            const testInput = DateTime.fromObject(
                {
                    year: 2023,
                    month: 1,
                    day: 10,
                    hour: 6,
                },
                { zone: "UTC" }
            );

            expect(timeMonth.ceil(testInput).hour).toEqual(0);
            expect(timeMonth.ceil(testInput).minute).toEqual(0);
            expect(timeMonth.ceil(testInput).day).toEqual(1);
        });

        it("should return expected changed datetime object using offset", () => {
            const testInput = DateTime.fromObject(
                {
                    year: 2023,
                    month: 1,
                    day: 1,
                    hour: 6,
                },
                { zone: "UTC" }
            );

            expect(timeMonth.offset(testInput, 1).hour).toEqual(6);
            expect(timeMonth.offset(testInput, 1).day).toEqual(1);
            expect(timeMonth.offset(testInput, 1).month).toEqual(2);
        });

        it("should return expected changed datetime object using range", () => {
            const testStart = DateTime.fromObject(
                {
                    year: 2023,
                    month: 1,
                    day: 1,
                    hour: 6,
                },
                { zone: "UTC" }
            );
            const testEnd = DateTime.fromObject(
                {
                    year: 2023,
                    month: 4,
                    day: 6,
                    hour: 18,
                },
                { zone: "UTC" }
            );

            const result = timeMonth.range(testStart, testEnd);

            expect(result).toHaveLength(3);
            expect(result[0].second).toEqual(0);
            expect(result[0].minute).toEqual(0);
            expect(result[0].hour).toEqual(0);

            // Because the start date is not top of day
            expect(result[0].month).toEqual(2);
            expect(result[2].month).toEqual(4);
        });
    });
});
