import i18n from "i18next";
import { DateTime, Settings } from "luxon";
import {
    TimeRange,
    TimeRangeDuration,
} from "../../components/time-range-selector/time-range-selector.interfaces";
import * as dateTimeUtil from "../date-time-util/date-time-util";
import {
    createTimeRangeDuration,
    formatTimeRange,
    formatTimeRangeDuration,
    getTimeRangeDuration,
} from "./time-range-util";

jest.mock("i18next");
jest.mock("../date-time-util/date-time-util");

const locale = Settings.defaultLocale;
const zoneName = Settings.defaultZoneName;

describe("Time Range Util", () => {
    beforeAll(() => {
        // Make sure date time manipulations and literal results are consistent regardless of where
        // tests are run by explicitly locale and setting time zone
        Settings.defaultLocale = "en-US";
        Settings.defaultZoneName = "America/Los_Angeles";

        jest.spyOn(DateTime, "local").mockImplementation(
            (): DateTime => {
                return DateTime.fromMillis(1606852800000); // December 1, 2020, 12:00:00 PM
            }
        );

        i18n.t = jest.fn().mockImplementation((key: string): string => {
            return key;
        });

        jest.spyOn(dateTimeUtil, "formatDateAndTime").mockImplementation(
            (date: number): string => {
                return date.toString();
            }
        );
    });

    beforeEach(() => {
        jest.clearAllMocks();
    });

    afterAll(() => {
        Settings.defaultLocale = locale;
        Settings.defaultZoneName = zoneName;

        jest.restoreAllMocks();
    });

    test("createTimeRangeDuration shall create appropriate time range with default inputs", () => {
        const timeRange = createTimeRangeDuration(TimeRange.TODAY);

        expect(timeRange.timeRange).toEqual(TimeRange.TODAY);
        expect(timeRange.startTime).toEqual(0);
        expect(timeRange.endTime).toEqual(0);
    });

    test("createTimeRangeDuration shall create appropriate time range", () => {
        const timeRange = createTimeRangeDuration(TimeRange.CUSTOM, 1, 2);

        expect(timeRange.timeRange).toEqual(TimeRange.CUSTOM);
        expect(timeRange.startTime).toEqual(1);
        expect(timeRange.endTime).toEqual(2);
    });

    test("getTimeRangeDuration shall return appropriate TimeRange.LAST_15_MINUTES time range", () => {
        expect(getTimeRangeDuration(TimeRange.LAST_15_MINUTES)).toEqual({
            timeRange: TimeRange.LAST_15_MINUTES,
            startTime: 1606851900000,
            endTime: 1606852800000,
        });
    });

    test("getTimeRangeDuration shall return appropriate TimeRange.LAST_1_HOUR time range", () => {
        expect(getTimeRangeDuration(TimeRange.LAST_1_HOUR)).toEqual({
            timeRange: TimeRange.LAST_1_HOUR,
            startTime: 1606849200000,
            endTime: 1606852800000,
        });
    });

    test("getTimeRangeDuration shall return appropriate TimeRange.LAST_12_HOURS time range", () => {
        expect(getTimeRangeDuration(TimeRange.LAST_12_HOURS)).toEqual({
            timeRange: TimeRange.LAST_12_HOURS,
            startTime: 1606809600000,
            endTime: 1606852800000,
        });
    });

    test("getTimeRangeDuration shall return appropriate TimeRange.LAST_24_HOURS time range", () => {
        expect(getTimeRangeDuration(TimeRange.LAST_24_HOURS)).toEqual({
            timeRange: TimeRange.LAST_24_HOURS,
            startTime: 1606766400000,
            endTime: 1606852800000,
        });
    });

    test("getTimeRangeDuration shall return appropriate TimeRange.LAST_7_DAYS time range", () => {
        expect(getTimeRangeDuration(TimeRange.LAST_7_DAYS)).toEqual({
            timeRange: TimeRange.LAST_7_DAYS,
            startTime: 1606248000000,
            endTime: 1606852800000,
        });
    });

    test("getTimeRangeDuration shall return appropriate TimeRange.LAST_30_DAYS time range", () => {
        expect(getTimeRangeDuration(TimeRange.LAST_30_DAYS)).toEqual({
            timeRange: TimeRange.LAST_30_DAYS,
            startTime: 1604260800000,
            endTime: 1606852800000,
        });
    });

    test("getTimeRangeDuration shall return appropriate TimeRange.TODAY time range", () => {
        expect(getTimeRangeDuration(TimeRange.TODAY)).toEqual({
            timeRange: TimeRange.TODAY,
            startTime: 1606809600000,
            endTime: 1606852800000,
        });
    });

    test("getTimeRangeDuration shall return appropriate TimeRange.YESTERDAY time range", () => {
        expect(getTimeRangeDuration(TimeRange.YESTERDAY)).toEqual({
            timeRange: TimeRange.YESTERDAY,
            startTime: 1606723200000,
            endTime: 1606809599999,
        });
    });

    test("getTimeRangeDuration shall return appropriate TimeRange.THIS_WEEK time range", () => {
        expect(getTimeRangeDuration(TimeRange.THIS_WEEK)).toEqual({
            timeRange: TimeRange.THIS_WEEK,
            startTime: 1606723200000,
            endTime: 1606852800000,
        });
    });

    test("getTimeRangeDuration shall return appropriate TimeRange.LAST_WEEK time range", () => {
        expect(getTimeRangeDuration(TimeRange.LAST_WEEK)).toEqual({
            timeRange: TimeRange.LAST_WEEK,
            startTime: 1606118400000,
            endTime: 1606723199999,
        });
    });

    test("getTimeRangeDuration shall return appropriate TimeRange.THIS_MONTH time range", () => {
        expect(getTimeRangeDuration(TimeRange.THIS_MONTH)).toEqual({
            timeRange: TimeRange.THIS_MONTH,
            startTime: 1606809600000,
            endTime: 1606852800000,
        });
    });

    test("getTimeRangeDuration shall return appropriate TimeRange.LAST_MONTH time range", () => {
        expect(getTimeRangeDuration(TimeRange.LAST_MONTH)).toEqual({
            timeRange: TimeRange.LAST_MONTH,
            startTime: 1604214000000,
            endTime: 1606809599999,
        });
    });

    test("getTimeRangeDuration shall return appropriate TimeRange.THIS_YEAR time range", () => {
        expect(getTimeRangeDuration(TimeRange.THIS_YEAR)).toEqual({
            timeRange: TimeRange.THIS_YEAR,
            startTime: 1577865600000,
            endTime: 1606852800000,
        });
    });

    test("getTimeRangeDuration shall return appropriate TimeRange.LAST_YEAR time range", () => {
        expect(getTimeRangeDuration(TimeRange.LAST_YEAR)).toEqual({
            timeRange: TimeRange.LAST_YEAR,
            startTime: 1546329600000,
            endTime: 1577865599999,
        });
    });

    test("getTimeRangeDuration shall return default TimeRange.TODAY time range for TimeRange.CUSTOM", () => {
        expect(getTimeRangeDuration(TimeRange.CUSTOM)).toEqual({
            timeRange: TimeRange.TODAY,
            startTime: 1606809600000,
            endTime: 1606852800000,
        });
    });

    test("formatTimeRange shall return empty string for invalid time range", () => {
        expect(formatTimeRange((null as unknown) as TimeRange)).toEqual("");
    });

    test("formatTimeRange shall return appropriate string for valid time range", () => {
        expect(formatTimeRange(TimeRange.LAST_12_HOURS)).toEqual(
            "label.last-12-hours"
        );
    });

    test("formatTimeRangeDuration shall return empty string for invalid time range duration", () => {
        expect(
            formatTimeRangeDuration((null as unknown) as TimeRangeDuration)
        ).toEqual("");
    });

    test("formatTimeRangeDuration shall return appropriate string for valid time range duration", () => {
        const mockTimeRange: TimeRangeDuration = {
            timeRange: TimeRange.LAST_12_HOURS,
            startTime: 0,
            endTime: 0,
        };

        expect(formatTimeRangeDuration(mockTimeRange)).toEqual(
            "label.last-12-hours"
        );
    });

    test("formatTimeRangeDuration shall return appropriate string for valid custom time range duration", () => {
        const mockCustomTimeRange: TimeRangeDuration = {
            timeRange: TimeRange.CUSTOM,
            startTime: 1,
            endTime: 2,
        };

        expect(formatTimeRangeDuration(mockCustomTimeRange)).toEqual(
            "label.start-time-end-time"
        );
        expect(i18n.t).toHaveBeenCalledWith("label.start-time-end-time", {
            startTime: "1",
            endTime: "2",
        });
    });
});
