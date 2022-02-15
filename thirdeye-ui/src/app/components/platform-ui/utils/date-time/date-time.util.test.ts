// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { Settings } from "luxon";
import {
    formatDateAndTimeV1,
    formatDateV1,
    formatDayV1,
    formatDurationV1,
    formatHourV1,
    formatMeridiemV1,
    formatMillisecondV1,
    formatMinuteV1,
    formatMonthOfYearV1,
    formatMonthV1,
    formatSecondV1,
    formatTimeV1,
    formatYearV1,
    switchMeridiemV1,
} from "./date-time.util";

const systemLocale = Settings.defaultLocale;
const systemZoneName = Settings.defaultZoneName;

jest.mock("../number/number.util", () => ({
    formatNumberV1: jest.fn().mockImplementation((num) => num.toString()),
}));

jest.mock("i18next", () => ({
    t: jest.fn().mockImplementation((key) => key),
}));

describe("Date Time Util", () => {
    beforeAll(() => {
        // Explicitly set locale and time zone to make sure date time manipulations and literal
        // results are consistent regardless of where tests are run
        Settings.defaultLocale = "en-US";
        Settings.defaultZoneName = "America/Los_Angeles";
    });

    afterAll(() => {
        // Restore locale and time zone
        Settings.defaultLocale = systemLocale;
        Settings.defaultZoneName = systemZoneName;
    });

    it("formatDurationV1 should return empty string for invalid start and end time", () => {
        expect(formatDurationV1(null as unknown as number, 1)).toEqual("");
        expect(formatDurationV1(1, null as unknown as number)).toEqual("");
        expect(
            formatDurationV1(
                null as unknown as number,
                null as unknown as number
            )
        ).toEqual("");
    });

    it("formatDurationV1 should return appropriate string for start and end time duration in years", () => {
        expect(formatDurationV1(1577865600000, 1609401600000)).toEqual(
            "1 label.year-lowercase"
        );
        expect(formatDurationV1(1577865600000, 1640937600000)).toEqual(
            "2 label.years-lowercase"
        );
    });

    it("formatDurationV1 should return appropriate string for start and end time duration in months", () => {
        expect(formatDurationV1(1577865600000, 1580457600000)).toEqual(
            "1 label.month-lowercase"
        );
        expect(formatDurationV1(1577865600000, 1583136000000)).toEqual(
            "2 label.months-lowercase"
        );
    });

    it("formatDurationV1 should return appropriate string for start and end time duration in weeks", () => {
        expect(formatDurationV1(1577865600000, 1578470400000)).toEqual(
            "1 label.week-lowercase"
        );
        expect(formatDurationV1(1577865600000, 1579075200000)).toEqual(
            "2 label.weeks-lowercase"
        );
    });

    it("formatDurationV1 should return appropriate string for start and end time duration in days", () => {
        expect(formatDurationV1(1577865600000, 1577952000000)).toEqual(
            "1 label.day-lowercase"
        );
        expect(formatDurationV1(1577865600000, 1578038400000)).toEqual(
            "2 label.days-lowercase"
        );
    });

    it("formatDurationV1 should return appropriate string for start and end time duration in hours", () => {
        expect(formatDurationV1(1577865600000, 1577869200000)).toEqual(
            "1 label.hour-lowercase"
        );
        expect(formatDurationV1(1577865600000, 1577872800000)).toEqual(
            "2 label.hours-lowercase"
        );
    });

    it("formatDurationV1 should return appropriate string for start and end time duration in minutes", () => {
        expect(formatDurationV1(1577865600000, 1577865660000)).toEqual(
            "1 label.minute-lowercase"
        );
        expect(formatDurationV1(1577865600000, 1577865720000)).toEqual(
            "2 label.minutes-lowercase"
        );
    });

    it("formatDurationV1 should return appropriate string for start and end time duration in seconds", () => {
        expect(formatDurationV1(1577865600000, 1577865601000)).toEqual(
            "1 label.second-lowercase"
        );
        expect(formatDurationV1(1577865600000, 1577865602000)).toEqual(
            "2 label.seconds-lowercase"
        );
    });

    it("formatDurationV1 should return appropriate string for start and end time duration in milliseconds", () => {
        expect(formatDurationV1(1577865600000, 1577865600001)).toEqual(
            "1 label.millisecond-lowercase"
        );
        expect(formatDurationV1(1577865600000, 1577865600002)).toEqual(
            "2 label.milliseconds-lowercase"
        );
        expect(formatDurationV1(1577865600000, 1577865600000)).toEqual(
            "0 label.milliseconds-lowercase"
        );
    });

    it("formatDateAndTimeV1 should return empty string for invalid date", () => {
        expect(formatDateAndTimeV1(null as unknown as number)).toEqual("");
    });

    it("formatDateAndTimeV1 should return appropriate string for date", () => {
        expect(formatDateAndTimeV1(1577865600000)).toEqual(
            "Jan 01, 2020, 12:00 AM"
        );
    });

    it("formatDateV1 should return empty string for invalid date", () => {
        expect(formatDateV1(null as unknown as number)).toEqual("");
    });

    it("formatDateV1 should return appropriate string for date", () => {
        expect(formatDateV1(1577865600000)).toEqual("Jan 01, 2020");
    });

    it("formatTimeV1 should return empty string for invalid date", () => {
        expect(formatTimeV1(null as unknown as number)).toEqual("");
    });

    it("formatTimeV1 should return appropriate string for date", () => {
        expect(formatTimeV1(1577865600000)).toEqual("12:00 AM");
    });

    it("formatYearV1 should return empty string for invalid date", () => {
        expect(formatYearV1(null as unknown as number)).toEqual("");
    });

    it("formatYearV1 should return appropriate string for date", () => {
        expect(formatYearV1(1577865600000)).toEqual("2020");
    });

    it("formatMonthV1 should return empty string for invalid date", () => {
        expect(formatMonthV1(null as unknown as number)).toEqual("");
    });

    it("formatMonthV1 should return appropriate string for date", () => {
        expect(formatMonthV1(1577865600000)).toEqual("Jan");
    });

    it("formatMonthOfYearV1 should return empty string for invalid date", () => {
        expect(formatMonthOfYearV1(null as unknown as number)).toEqual("");
    });

    it("formatMonthOfYearV1 should return appropriate string for date", () => {
        expect(formatMonthOfYearV1(1577865600000)).toEqual("Jan 2020");
    });

    it("formatDayV1 should return empty string for invalid date", () => {
        expect(formatDayV1(null as unknown as number)).toEqual("");
    });

    it("formatDayV1 should return appropriate string for date", () => {
        expect(formatDayV1(1577865600000)).toEqual("01");
    });

    it("formatHourV1 should return empty string for invalid date", () => {
        expect(formatHourV1(null as unknown as number)).toEqual("");
    });

    it("formatHourV1 should return appropriate string for date", () => {
        expect(formatHourV1(1577865600000)).toEqual("12");
    });

    it("formatMinuteV1 should return empty string for invalid date", () => {
        expect(formatMinuteV1(null as unknown as number)).toEqual("");
    });

    it("formatMinuteV1 should return appropriate string for date", () => {
        expect(formatMinuteV1(1577865600000)).toEqual("00");
    });

    it("formatSecondV1 should return empty string for invalid date", () => {
        expect(formatSecondV1(null as unknown as number)).toEqual("");
    });

    it("formatSecondV1 should return appropriate string for date", () => {
        expect(formatSecondV1(1577865600000)).toEqual("00");
    });

    it("formatMillisecondV1 should return empty string for invalid date", () => {
        expect(formatMillisecondV1(null as unknown as number)).toEqual("");
    });

    it("formatMillisecondV1 should return appropriate string for date", () => {
        expect(formatMillisecondV1(1577865600000)).toEqual("000");
    });

    it("formatMeridiemV1 should return empty string for invalid date", () => {
        expect(formatMeridiemV1(null as unknown as number)).toEqual("");
    });

    it("formatMeridiem should return appropriate string for date", () => {
        expect(formatMeridiemV1(1577865600000)).toEqual("AM");
    });

    it("switchMeridiemV1 should return -1 for invalid date", () => {
        expect(switchMeridiemV1(null as unknown as number)).toEqual(-1);
    });

    it("switchMeridiemV1 should return appropriate date with switched meridiem for date", () => {
        expect(switchMeridiemV1(1577865600000)).toEqual(1577908800000);
        expect(switchMeridiemV1(1577908800000)).toEqual(1577865600000);
    });
});
