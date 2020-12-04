import { DateTime } from "luxon";
import { TimeRangeType } from "../../components/time-range/time-range-selector.interfaces";
import { createTimeRange, getTimeRange } from "./time-range-util";

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

    test("createTimeRange shall create appropriate time range", () => {
        const timeRange = createTimeRange(TimeRangeType.CUSTOM, 1, 2);

        expect(timeRange.type).toEqual(TimeRangeType.CUSTOM);
        expect(timeRange.startTime).toEqual(1);
        expect(timeRange.endTime).toEqual(2);
    });

    test("getTimeRange shall return appropriate TimeRangeType.LAST_15_MINUTES time range", () => {
        expect(getTimeRange(TimeRangeType.LAST_15_MINUTES)).toEqual({
            type: TimeRangeType.LAST_15_MINUTES,
            startTime: 1606851900000,
            endTime: 1606852800000,
        });
    });

    test("getTimeRange shall return appropriate TimeRangeType.LAST_1_HOUR time range", () => {
        expect(getTimeRange(TimeRangeType.LAST_1_HOUR)).toEqual({
            type: TimeRangeType.LAST_1_HOUR,
            startTime: 1606849200000,
            endTime: 1606852800000,
        });
    });

    test("getTimeRange shall return appropriate TimeRangeType.LAST_12_HOURS time range", () => {
        expect(getTimeRange(TimeRangeType.LAST_12_HOURS)).toEqual({
            type: TimeRangeType.LAST_12_HOURS,
            startTime: 1606809600000,
            endTime: 1606852800000,
        });
    });

    test("getTimeRange shall return appropriate TimeRangeType.LAST_24_HOURS time range", () => {
        expect(getTimeRange(TimeRangeType.LAST_24_HOURS)).toEqual({
            type: TimeRangeType.LAST_24_HOURS,
            startTime: 1606766400000,
            endTime: 1606852800000,
        });
    });

    test("getTimeRange shall return appropriate TimeRangeType.LAST_7_DAYS time range", () => {
        expect(getTimeRange(TimeRangeType.LAST_7_DAYS)).toEqual({
            type: TimeRangeType.LAST_7_DAYS,
            startTime: 1606248000000,
            endTime: 1606852800000,
        });
    });

    test("getTimeRange shall return appropriate TimeRangeType.LAST_30_DAYS time range", () => {
        expect(getTimeRange(TimeRangeType.LAST_30_DAYS)).toEqual({
            type: TimeRangeType.LAST_30_DAYS,
            startTime: 1604260800000,
            endTime: 1606852800000,
        });
    });

    test("getTimeRange shall return appropriate TimeRangeType.TODAY time range", () => {
        expect(getTimeRange(TimeRangeType.TODAY)).toEqual({
            type: TimeRangeType.TODAY,
            startTime: 1606809600000,
            endTime: 1606852800000,
        });
    });

    test("getTimeRange shall return appropriate TimeRangeType.YESTERDAY time range", () => {
        expect(getTimeRange(TimeRangeType.YESTERDAY)).toEqual({
            type: TimeRangeType.YESTERDAY,
            startTime: 1606723200000,
            endTime: 1606809599999,
        });
    });

    test("getTimeRange shall return appropriate TimeRangeType.THIS_WEEK time range", () => {
        expect(getTimeRange(TimeRangeType.THIS_WEEK)).toEqual({
            type: TimeRangeType.THIS_WEEK,
            startTime: 1606723200000,
            endTime: 1606852800000,
        });
    });

    test("getTimeRange shall return appropriate TimeRangeType.LAST_WEEK time range", () => {
        expect(getTimeRange(TimeRangeType.LAST_WEEK)).toEqual({
            type: TimeRangeType.LAST_WEEK,
            startTime: 1606118400000,
            endTime: 1606723199999,
        });
    });

    test("getTimeRange shall return appropriate TimeRangeType.THIS_MONTH time range", () => {
        expect(getTimeRange(TimeRangeType.THIS_MONTH)).toEqual({
            type: TimeRangeType.THIS_MONTH,
            startTime: 1606809600000,
            endTime: 1606852800000,
        });
    });

    test("getTimeRange shall return appropriate TimeRangeType.LAST_MONTH time range", () => {
        expect(getTimeRange(TimeRangeType.LAST_MONTH)).toEqual({
            type: TimeRangeType.LAST_MONTH,
            startTime: 1604214000000,
            endTime: 1606809599999,
        });
    });

    test("getTimeRange shall return appropriate TimeRangeType.THIS_YEAR time range", () => {
        expect(getTimeRange(TimeRangeType.THIS_YEAR)).toEqual({
            type: TimeRangeType.THIS_YEAR,
            startTime: 1577865600000,
            endTime: 1606852800000,
        });
    });

    test("getTimeRange shall return appropriate TimeRangeType.LAST_YEAR time range", () => {
        expect(getTimeRange(TimeRangeType.LAST_YEAR)).toEqual({
            type: TimeRangeType.LAST_YEAR,
            startTime: 1546329600000,
            endTime: 1577865599999,
        });
    });

    test("getTimeRange shall return default TimeRangeType.TODAY time range for TimeRangeType.CUSTOM", () => {
        expect(getTimeRange(TimeRangeType.CUSTOM)).toEqual({
            type: TimeRangeType.TODAY,
            startTime: 1606809600000,
            endTime: 1606852800000,
        });
    });
});
