import { Settings } from "luxon";
import {
    formatDate,
    formatDateAndTime,
    formatDay,
    formatDuration,
    formatHour,
    formatMeridiem,
    formatMillisecond,
    formatMinute,
    formatMonth,
    formatMonthOfYear,
    formatSecond,
    formatTime,
    formatYear,
    switchMeridiem,
} from "./date-time.util";

const systemLocale = Settings.defaultLocale;
const systemZoneName = Settings.defaultZoneName;

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

    test("formatDuration should return empty string for invalid start and end time", () => {
        expect(formatDuration((null as unknown) as number, 1)).toEqual("");
        expect(formatDuration(1, (null as unknown) as number)).toEqual("");
        expect(
            formatDuration(
                (null as unknown) as number,
                (null as unknown) as number
            )
        ).toEqual("");
    });

    test("formatDuration should return appropriate string for start and end time duration in years", () => {
        expect(formatDuration(1577865600000, 1609401600000)).toEqual(
            "1 label.year-lowercase"
        );
        expect(formatDuration(1577865600000, 1640937600000)).toEqual(
            "2 label.years-lowercase"
        );
    });

    test("formatDuration should return appropriate string for start and end time duration in months", () => {
        expect(formatDuration(1577865600000, 1580457600000)).toEqual(
            "1 label.month-lowercase"
        );
        expect(formatDuration(1577865600000, 1583136000000)).toEqual(
            "2 label.months-lowercase"
        );
    });

    test("formatDuration should return appropriate string for start and end time duration in weeks", () => {
        expect(formatDuration(1577865600000, 1578470400000)).toEqual(
            "1 label.week-lowercase"
        );
        expect(formatDuration(1577865600000, 1579075200000)).toEqual(
            "2 label.weeks-lowercase"
        );
    });

    test("formatDuration should return appropriate string for start and end time duration in days", () => {
        expect(formatDuration(1577865600000, 1577952000000)).toEqual(
            "1 label.day-lowercase"
        );
        expect(formatDuration(1577865600000, 1578038400000)).toEqual(
            "2 label.days-lowercase"
        );
    });

    test("formatDuration should return appropriate string for start and end time duration in hours", () => {
        expect(formatDuration(1577865600000, 1577869200000)).toEqual(
            "1 label.hour-lowercase"
        );
        expect(formatDuration(1577865600000, 1577872800000)).toEqual(
            "2 label.hours-lowercase"
        );
    });

    test("formatDuration should return appropriate string for start and end time duration in minutes", () => {
        expect(formatDuration(1577865600000, 1577865660000)).toEqual(
            "1 label.minute-lowercase"
        );
        expect(formatDuration(1577865600000, 1577865720000)).toEqual(
            "2 label.minutes-lowercase"
        );
    });

    test("formatDuration should return appropriate string for start and end time duration in seconds", () => {
        expect(formatDuration(1577865600000, 1577865601000)).toEqual(
            "1 label.second-lowercase"
        );
        expect(formatDuration(1577865600000, 1577865602000)).toEqual(
            "2 label.seconds-lowercase"
        );
    });

    test("formatDuration should return appropriate string for start and end time duration in milliseconds", () => {
        expect(formatDuration(1577865600000, 1577865600001)).toEqual(
            "1 label.millisecond-lowercase"
        );
        expect(formatDuration(1577865600000, 1577865600002)).toEqual(
            "2 label.milliseconds-lowercase"
        );
        expect(formatDuration(1577865600000, 1577865600000)).toEqual(
            "0 label.milliseconds-lowercase"
        );
    });

    test("formatDateAndTime should return empty string for invalid date", () => {
        expect(formatDateAndTime((null as unknown) as number)).toEqual("");
    });

    test("formatDateAndTime should return appropriate string for date", () => {
        expect(formatDateAndTime(1577865600000)).toEqual(
            "Jan 01, 2020, 12:00 AM"
        );
    });

    test("formatDate should return empty string for invalid date", () => {
        expect(formatDate((null as unknown) as number)).toEqual("");
    });

    test("formatDate should return appropriate string for date", () => {
        expect(formatDate(1577865600000)).toEqual("Jan 01, 2020");
    });

    test("formatTime should return empty string for invalid date", () => {
        expect(formatTime((null as unknown) as number)).toEqual("");
    });

    test("formatTime should return appropriate string for date", () => {
        expect(formatTime(1577865600000)).toEqual("12:00 AM");
    });

    test("formatYear should return empty string for invalid date", () => {
        expect(formatYear((null as unknown) as number)).toEqual("");
    });

    test("formatYear should return appropriate string for date", () => {
        expect(formatYear(1577865600000)).toEqual("2020");
    });

    test("formatMonth should return empty string for invalid date", () => {
        expect(formatMonth((null as unknown) as number)).toEqual("");
    });

    test("formatMonth should return appropriate string for date", () => {
        expect(formatMonth(1577865600000)).toEqual("Jan");
    });

    test("formatMonthOfYear should return empty string for invalid date", () => {
        expect(formatMonthOfYear((null as unknown) as number)).toEqual("");
    });

    test("formatMonthOfYear should return appropriate string for date", () => {
        expect(formatMonthOfYear(1577865600000)).toEqual("Jan 2020");
    });

    test("formatDay should return empty string for invalid date", () => {
        expect(formatDay((null as unknown) as number)).toEqual("");
    });

    test("formatDay should return appropriate string for date", () => {
        expect(formatDay(1577865600000)).toEqual("01");
    });

    test("formatHour should return empty string for invalid date", () => {
        expect(formatHour((null as unknown) as number)).toEqual("");
    });

    test("formatHour should return appropriate string for date", () => {
        expect(formatHour(1577865600000)).toEqual("12");
    });

    test("formatMinute should return empty string for invalid date", () => {
        expect(formatMinute((null as unknown) as number)).toEqual("");
    });

    test("formatMinute should return appropriate string for date", () => {
        expect(formatMinute(1577865600000)).toEqual("00");
    });

    test("formatSecond should return empty string for invalid date", () => {
        expect(formatSecond((null as unknown) as number)).toEqual("");
    });

    test("formatSecond should return appropriate string for date", () => {
        expect(formatSecond(1577865600000)).toEqual("00");
    });

    test("formatMillisecond should return empty string for invalid date", () => {
        expect(formatMillisecond((null as unknown) as number)).toEqual("");
    });

    test("formatMillisecond should return appropriate string for date", () => {
        expect(formatMillisecond(1577865600000)).toEqual("000");
    });

    test("formatMeridiem should return empty string for invalid date", () => {
        expect(formatMeridiem((null as unknown) as number)).toEqual("");
    });

    test("formatMeridiem should return appropriate string for date", () => {
        expect(formatMeridiem(1577865600000)).toEqual("AM");
    });

    test("switchMeridiem should return -1 for invalid date", () => {
        expect(switchMeridiem((null as unknown) as number)).toEqual(-1);
    });

    test("switchMeridiem should return appropriate date with switched meridiem for date", () => {
        expect(switchMeridiem(1577865600000)).toEqual(1577908800000);
        expect(switchMeridiem(1577908800000)).toEqual(1577865600000);
    });
});
