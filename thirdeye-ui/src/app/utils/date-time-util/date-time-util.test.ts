import i18n from "i18next";
import { Settings } from "luxon";
import {
    formatDate,
    formatDateAndTime,
    formatDay,
    formatDuration,
    formatHour,
    formatMeridiem,
    formatMinute,
    formatMonth,
    formatMonthOfYear,
    formatTime,
    formatYear,
    switchMeridiem,
} from "./date-time-util";

jest.mock("i18next");

const locale = Settings.defaultLocale;
const zoneName = Settings.defaultZoneName;

describe("Date Time Util", () => {
    beforeAll(() => {
        // Make sure date time manipulations and literal results are consistent regardless of where
        // tests are run by explicitly locale and setting time zone
        Settings.defaultLocale = "en-US";
        Settings.defaultZoneName = "America/Los_Angeles";

        i18n.t = jest.fn().mockImplementation((key: string): string => {
            return key;
        });
    });

    beforeEach(() => {
        jest.clearAllMocks();
    });

    afterAll(() => {
        Settings.defaultLocale = locale;
        Settings.defaultZoneName = zoneName;

        jest.restoreAllMocks();
    });

    test("formatDuration shall return empty string for invalid input", () => {
        expect(formatDuration((null as unknown) as number, 2)).toEqual("");
        expect(formatDuration(1, (null as unknown) as number)).toEqual("");
        expect(
            formatDuration(
                (null as unknown) as number,
                (null as unknown) as number
            )
        ).toEqual("");
    });

    test("formatDuration shall return appropriate duration in years", () => {
        expect(formatDuration(1575230400000, 1606852800000)).toEqual(
            "1 label.year-lowercase"
        );
        expect(formatDuration(1543694400000, 1606852800000)).toEqual(
            "2 label.years-lowercase"
        );
    });

    test("formatDuration shall return appropriate duration in months", () => {
        expect(formatDuration(1604260800000, 1606852800000)).toEqual(
            "1 label.month-lowercase"
        );
        expect(formatDuration(1601578800000, 1606852800000)).toEqual(
            "2 label.months-lowercase"
        );
    });

    test("formatDuration shall return appropriate duration in weeks", () => {
        expect(formatDuration(1606248000000, 1606852800000)).toEqual(
            "1 label.week-lowercase"
        );
        expect(formatDuration(1605643200000, 1606852800000)).toEqual(
            "2 label.weeks-lowercase"
        );
    });

    test("formatDuration shall return appropriate duration in days", () => {
        expect(formatDuration(1606766400000, 1606852800000)).toEqual(
            "1 label.day-lowercase"
        );
        expect(formatDuration(1606680000000, 1606852800000)).toEqual(
            "2 label.days-lowercase"
        );
    });

    test("formatDuration shall return appropriate duration in hours", () => {
        expect(formatDuration(1606849200000, 1606852800000)).toEqual(
            "1 label.hour-lowercase"
        );
        expect(formatDuration(1606845600000, 1606852800000)).toEqual(
            "2 label.hours-lowercase"
        );
    });

    test("formatDuration shall return appropriate duration in minutes", () => {
        expect(formatDuration(1606852740000, 1606852800000)).toEqual(
            "1 label.minute-lowercase"
        );
        expect(formatDuration(1606852680000, 1606852800000)).toEqual(
            "2 label.minutes-lowercase"
        );
    });

    test("formatDuration shall return appropriate duration in seconds", () => {
        expect(formatDuration(1606852799000, 1606852800000)).toEqual(
            "1 label.second-lowercase"
        );
        expect(formatDuration(1606852798000, 1606852800000)).toEqual(
            "2 label.seconds-lowercase"
        );
    });

    test("formatDuration shall return appropriate duration in milliseconds", () => {
        expect(formatDuration(1606852799999, 1606852800000)).toEqual(
            "1 label.millisecond-lowercase"
        );
        expect(formatDuration(1606852799998, 1606852800000)).toEqual(
            "2 label.milliseconds-lowercase"
        );
        expect(formatDuration(1606852800000, 1606852800000)).toEqual(
            "0 label.milliseconds-lowercase"
        );
    });

    test("formatDateAndTime shall return empty string for invalid input", () => {
        expect(formatDateAndTime((null as unknown) as number)).toEqual("");
    });

    test("formatDateAndTime shall return appropriate formatted date and time", () => {
        expect(formatDateAndTime(1606852800000)).toEqual(
            "Dec 01, 2020, 12:00 PM"
        );
    });

    test("formatDate shall return empty string for invalid input", () => {
        expect(formatDate((null as unknown) as number)).toEqual("");
    });

    test("formatDate shall return appropriate formatted date part in given date", () => {
        expect(formatDate(1606852800000)).toEqual("Dec 01, 2020");
    });

    test("formatTime shall return empty string for invalid input", () => {
        expect(formatTime((null as unknown) as number)).toEqual("");
    });

    test("formatTime shall return appropriate formatted time part in given date", () => {
        expect(formatTime(1606852800000)).toEqual("12:00 PM");
    });

    test("formatYear shall return empty string for invalid input", () => {
        expect(formatYear((null as unknown) as number)).toEqual("");
    });

    test("formatYear shall return appropriate formatted year in given date", () => {
        expect(formatYear(1606852800000)).toEqual("2020");
    });

    test("formatMonth shall return empty string for invalid input", () => {
        expect(formatMonth((null as unknown) as number)).toEqual("");
    });

    test("formatMonth shall return appropriate formatted month in given date", () => {
        expect(formatMonth(1606852800000)).toEqual("Dec");
    });

    test("formatMonthOfYear shall return empty string for invalid input", () => {
        expect(formatMonthOfYear((null as unknown) as number)).toEqual("");
    });

    test("formatMonthOfYear shall return appropriate formatted month with year in given date", () => {
        expect(formatMonthOfYear(1606852800000)).toEqual("Dec 2020");
    });

    test("formatDay shall return empty string for invalid input", () => {
        expect(formatDay((null as unknown) as number)).toEqual("");
    });

    test("formatDay shall return appropriate formatted day in given date", () => {
        expect(formatDay(1606852800000)).toEqual("01");
    });

    test("formatHour shall return empty string for invalid input", () => {
        expect(formatHour((null as unknown) as number)).toEqual("");
    });

    test("formatHour shall return appropriate formatted hour in given date", () => {
        expect(formatHour(1606852800000)).toEqual("12");
    });

    test("formatMinute shall return empty string for invalid input", () => {
        expect(formatMinute((null as unknown) as number)).toEqual("");
    });

    test("formatMinute shall return appropriate formatted minute in given date", () => {
        expect(formatMinute(1606852800000)).toEqual("00");
    });

    test("formatMeridiem shall return empty string for invalid input", () => {
        expect(formatMeridiem((null as unknown) as number)).toEqual("");
    });

    test("formatMeridiem shall return appropriate formatted time period in given date", () => {
        expect(formatMeridiem(1606852800000)).toEqual("PM");
    });

    test("switchMeridiem shall return -1 for invalid input", () => {
        expect(switchMeridiem((null as unknown) as number)).toEqual(-1);
    });

    test("switchMeridiem shall return date with switched meridiem as compared to given date", () => {
        expect(switchMeridiem(1606813200000)).toEqual(1606856400000);
        expect(switchMeridiem(1606856400000)).toEqual(1606813200000);
    });
});
