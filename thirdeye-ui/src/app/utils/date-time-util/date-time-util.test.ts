import i18n from "i18next";
import { formatDuration, formatLongDateAndTime } from "./date-time-util";

jest.mock("i18next");

describe("DateTime Util", () => {
    beforeAll(() => {
        i18n.t = jest.fn().mockImplementation((key: string): string => {
            return key;
        });
    });

    beforeEach(() => {
        jest.clearAllMocks();
    });

    afterAll(() => {
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

    test("formatLongDateAndTime shall return empty string for invalid input", () => {
        expect(formatLongDateAndTime((null as unknown) as number)).toEqual("");
    });

    test("formatLongDateAndTime shall invoke DateTime.fromJSDate with appropriate input and return result", () => {
        expect(formatLongDateAndTime(1606852800000)).toEqual(
            "Dec 01, 20, 12:00 PM"
        );
    });
});
