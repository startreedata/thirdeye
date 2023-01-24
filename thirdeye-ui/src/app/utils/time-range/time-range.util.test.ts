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
import { DateTime, Settings } from "luxon";
import {
    TimeRange,
    TimeRangeDuration,
} from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import {
    createTimeRangeDuration,
    formatTimeRange,
    formatTimeRangeDuration,
    getDefaultTimeRangeDuration,
    getTimeRangeDuration,
} from "./time-range.util";

const systemLocale = Settings.defaultLocale;
const systemZoneName = Settings.defaultZone;

jest.mock("i18next", () => ({
    t: jest.fn().mockImplementation((key) => key),
}));

jest.mock("../../platform/utils/date-time/date-time.util", () => ({
    formatDateAndTimeV1: jest
        .fn()
        .mockImplementation((date) => date.toString()),
}));

describe("Time Range Util", () => {
    beforeAll(() => {
        // Explicitly set locale and time zone to make sure date time manipulations and literal
        // results are consistent regardless of where tests are run
        Settings.defaultLocale = "en-US";
        Settings.defaultZone = "America/Los_Angeles";

        jest.spyOn(DateTime, "local").mockReturnValue(
            DateTime.fromMillis(1577865600000) // January 1, 2020, 12:00:00 AM
        );
    });

    afterAll(() => {
        // Restore locale and time zone
        Settings.defaultLocale = systemLocale;
        Settings.defaultZone = systemZoneName;
    });

    it("createTimeRangeDuration should create appropriate time range duration", () => {
        expect(createTimeRangeDuration(TimeRange.CUSTOM, 1, 2)).toEqual({
            timeRange: TimeRange.CUSTOM,
            startTime: 1,
            endTime: 2,
        });
    });

    it("getDefaultTimeRangeDuration should return appropriate TimeRange.TODAY time range duration", () => {
        expect(getDefaultTimeRangeDuration()).toEqual({
            timeRange: TimeRange.TODAY,
            startTime: 1577865600000,
            endTime: 1577865600000,
        });
    });

    it("getTimeRangeDuration should return default TimeRange.TODAY time range duration for invalid time range", () => {
        expect(getTimeRangeDuration(null as unknown as TimeRange)).toEqual({
            timeRange: TimeRange.TODAY,
            startTime: 1577865600000,
            endTime: 1577865600000,
        });
    });

    it("getTimeRangeDuration should return appropriate time range duration for TimeRange.LAST_15_MINUTES time range", () => {
        expect(getTimeRangeDuration(TimeRange.LAST_15_MINUTES)).toEqual({
            timeRange: TimeRange.LAST_15_MINUTES,
            startTime: 1577864700000,
            endTime: 1577865600000,
        });
    });

    it("getTimeRangeDuration should return appropriate time range duration for TimeRange.LAST_1_HOUR time range", () => {
        expect(getTimeRangeDuration(TimeRange.LAST_1_HOUR)).toEqual({
            timeRange: TimeRange.LAST_1_HOUR,
            startTime: 1577862000000,
            endTime: 1577865600000,
        });
    });

    it("getTimeRangeDuration should return appropriate time range duration for TimeRange.LAST_12_HOURS time range", () => {
        expect(getTimeRangeDuration(TimeRange.LAST_12_HOURS)).toEqual({
            timeRange: TimeRange.LAST_12_HOURS,
            startTime: 1577822400000,
            endTime: 1577865600000,
        });
    });

    it("getTimeRangeDuration should return appropriate time range duration for TimeRange.LAST_24_HOURS time range", () => {
        expect(getTimeRangeDuration(TimeRange.LAST_24_HOURS)).toEqual({
            timeRange: TimeRange.LAST_24_HOURS,
            startTime: 1577779200000,
            endTime: 1577865600000,
        });
    });

    it("getTimeRangeDuration should return appropriate time range duration for TimeRange.LAST_7_DAYS time range", () => {
        expect(getTimeRangeDuration(TimeRange.LAST_7_DAYS)).toEqual({
            timeRange: TimeRange.LAST_7_DAYS,
            startTime: 1577260800000,
            endTime: 1577865600000,
        });
    });

    it("getTimeRangeDuration should return appropriate time range duration for TimeRange.LAST_30_DAYS time range", () => {
        expect(getTimeRangeDuration(TimeRange.LAST_30_DAYS)).toEqual({
            timeRange: TimeRange.LAST_30_DAYS,
            startTime: 1575273600000,
            endTime: 1577865600000,
        });
    });

    it("getTimeRangeDuration should return appropriate time range duration for TimeRange.TODAY time range", () => {
        expect(getTimeRangeDuration(TimeRange.TODAY)).toEqual({
            timeRange: TimeRange.TODAY,
            startTime: 1577865600000,
            endTime: 1577865600000,
        });
    });

    it("getTimeRangeDuration should return appropriate time range duration for TimeRange.YESTERDAY time range", () => {
        expect(getTimeRangeDuration(TimeRange.YESTERDAY)).toEqual({
            timeRange: TimeRange.YESTERDAY,
            startTime: 1577779200000,
            endTime: 1577865599999,
        });
    });

    it("getTimeRangeDuration should return appropriate time range duration for TimeRange.THIS_WEEK time range", () => {
        expect(getTimeRangeDuration(TimeRange.THIS_WEEK)).toEqual({
            timeRange: TimeRange.THIS_WEEK,
            startTime: 1577692800000,
            endTime: 1577865600000,
        });
    });

    it("getTimeRangeDuration should return appropriate time range duration for TimeRange.LAST_WEEK time range", () => {
        expect(getTimeRangeDuration(TimeRange.LAST_WEEK)).toEqual({
            timeRange: TimeRange.LAST_WEEK,
            startTime: 1577088000000,
            endTime: 1577692799999,
        });
    });

    it("getTimeRangeDuration should return appropriate time range duration for TimeRange.THIS_MONTH time range", () => {
        expect(getTimeRangeDuration(TimeRange.THIS_MONTH)).toEqual({
            timeRange: TimeRange.THIS_MONTH,
            startTime: 1577865600000,
            endTime: 1577865600000,
        });
    });

    it("getTimeRangeDuration should return appropriate time range duration for TimeRange.LAST_MONTH time range", () => {
        expect(getTimeRangeDuration(TimeRange.LAST_MONTH)).toEqual({
            timeRange: TimeRange.LAST_MONTH,
            startTime: 1575187200000,
            endTime: 1577865599999,
        });
    });

    it("getTimeRangeDuration should return appropriate time range duration for TimeRange.THIS_YEAR time range", () => {
        expect(getTimeRangeDuration(TimeRange.THIS_YEAR)).toEqual({
            timeRange: TimeRange.THIS_YEAR,
            startTime: 1577865600000,
            endTime: 1577865600000,
        });
    });

    it("getTimeRangeDuration should return appropriate time range duration for TimeRange.LAST_YEAR time range", () => {
        expect(getTimeRangeDuration(TimeRange.LAST_YEAR)).toEqual({
            timeRange: TimeRange.LAST_YEAR,
            startTime: 1546329600000,
            endTime: 1577865599999,
        });
    });

    it("getTimeRangeDuration should return default TimeRange.TODAY time range duration for custom time range", () => {
        expect(getTimeRangeDuration(TimeRange.CUSTOM)).toEqual({
            timeRange: TimeRange.TODAY,
            startTime: 1577865600000,
            endTime: 1577865600000,
        });
    });

    it("formatTimeRange should return empty string for invalid time range", () => {
        expect(formatTimeRange(null as unknown as TimeRange)).toEqual("");
    });

    it("formatTimeRange should return appropriate string for time range", () => {
        expect(formatTimeRange(TimeRange.LAST_12_HOURS)).toEqual(
            "label.last-12-hours"
        );
    });

    it("formatTimeRangeDuration should return empty string for invalid time range duration", () => {
        expect(
            formatTimeRangeDuration(null as unknown as TimeRangeDuration)
        ).toEqual("");
    });

    it("formatTimeRangeDuration should return appropriate string for time range duration", () => {
        expect(formatTimeRangeDuration(mockCustomTimeRangeDuration)).toEqual(
            "label.start-time-end-time"
        );
        expect(i18n.t).toHaveBeenCalledWith("label.start-time-end-time", {
            startTime: "1",
            endTime: "2",
        });
    });
});

const mockCustomTimeRangeDuration = {
    timeRange: TimeRange.CUSTOM,
    startTime: 1,
    endTime: 2,
};
