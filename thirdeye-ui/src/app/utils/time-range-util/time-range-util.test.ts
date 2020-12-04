import { DateTime } from "luxon";
import { TimeRange } from "../../components/time-range-selector/time-range-selector.interfaces";
import {
    createTimeRangeDuration,
    getTimeRangeDuration,
} from "./time-range-util";

describe("Time Range Util", () => {
    beforeAll(() => {
        jest.spyOn(DateTime, "local").mockImplementation(
            (): DateTime => {
                return DateTime.fromMillis(1606852800000); // December 1, 2020, 12:00:00 PM
            }
        );
    });

    beforeEach(() => {
        jest.clearAllMocks();
    });

    afterAll(() => {
        jest.restoreAllMocks();
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
});
