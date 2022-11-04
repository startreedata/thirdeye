import { ScaleTime } from "d3-scale";
import { Settings } from "luxon";
import { DAY_IN_MILLISECONDS } from "../time/time.util";
import {
    checkIfOtherDimension,
    determineGranularity,
    formatDateTimeForTimeAxis,
    formatLargeNumberForVisualization,
    getTickValuesForTimeAxis,
} from "./visualization.util";

const systemLocale = Settings.defaultLocale;
const systemZoneName = Settings.defaultZoneName;

jest.mock("../../platform/utils", () => ({
    formatLargeNumberV1: jest.fn().mockImplementation((num) => num.toString()),
    formatYearV1: jest
        .fn()
        .mockImplementation((date) => `${date.toString()}year`),
    formatMonthOfYearV1: jest
        .fn()
        .mockImplementation((date) => `${date.toString()}monthOfYear`),
    formatDateV1: jest
        .fn()
        .mockImplementation((date) => `${date.toString()}date`),
    formatTimeV1: jest
        .fn()
        .mockImplementation((date) => `${date.toString()}time`),
}));

describe("Visualization Util", () => {
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

    it("formatLargeNumberForVisualization should return empty string for invalid number", () => {
        expect(
            formatLargeNumberForVisualization(null as unknown as number)
        ).toEqual("");
    });

    it("formatLargeNumberForVisualization should return empty string for invalid object", () => {
        expect(
            formatLargeNumberForVisualization({} as { valueOf(): number })
        ).toEqual("");
    });

    it("formatLargeNumberForVisualization should return appropriate string for number", () => {
        expect(formatLargeNumberForVisualization(1)).toEqual("1");
    });

    it("formatLargeNumberForVisualization should return appropriate string for object", () => {
        expect(formatLargeNumberForVisualization({ valueOf: () => 1 })).toEqual(
            "1"
        );
    });

    it("formatDateTimeForTimeAxis should return empty string for invalid date", () => {
        expect(
            formatDateTimeForTimeAxis(null as unknown as number, mockScale)
        ).toEqual("");
    });

    it("formatDateTimeForTimeAxis should return empty string for invalid object", () => {
        expect(
            formatDateTimeForTimeAxis({} as { valueOf(): number }, mockScale)
        ).toEqual("");
    });

    it("formatDateTimeForTimeAxis should return empty string for invalid scale", () => {
        expect(
            formatDateTimeForTimeAxis(
                1,
                null as unknown as ScaleTime<number, number>
            )
        ).toEqual("");
    });

    it("formatDateTimeForTimeAxis should return appropriate string for date and scale", () => {
        mockScaleDomain = [new Date(1577865600000), new Date(1672560000000)];

        expect(formatDateTimeForTimeAxis(1577865600000, mockScale)).toEqual(
            "1577865600000year"
        );
    });

    it("formatDateTimeForTimeAxis should return appropriate string for object and scale", () => {
        mockScaleDomain = [new Date(1577865600000), new Date(1672560000000)];

        expect(
            formatDateTimeForTimeAxis(
                { valueOf: () => 1577865600000 },
                mockScale
            )
        ).toEqual("1577865600000year");
    });

    it("formatDateTimeForTimeAxis should return appropriate string for date and scale domain interval of more than 2 years", () => {
        mockScaleDomain = [new Date(1577865600000), new Date(1672560000000)]; // Interval > 2 years

        expect(formatDateTimeForTimeAxis(1577865600000, mockScale)).toEqual(
            "1577865600000year"
        );
    });

    it("formatDateTimeForTimeAxis should return appropriate string for date and scale domain interval of less than or equal to 2 years and more than 2 months", () => {
        mockScaleDomain = [new Date(1577865600000), new Date(1640937600000)]; // Interval = 2 years

        expect(formatDateTimeForTimeAxis(1577865600000, mockScale)).toEqual(
            "1577865600000monthOfYear"
        );

        mockScaleDomain = [new Date(1577865600000), new Date(1585724400000)]; // 2 years >= interval > 2 months

        expect(formatDateTimeForTimeAxis(1577865600000, mockScale)).toEqual(
            "1577865600000monthOfYear"
        );
    });

    it("formatDateTimeForTimeAxis should return appropriate string for date and scale domain interval of less than or equal to 2 months and more than 2 days", () => {
        mockScaleDomain = [new Date(1577865600000), new Date(1583049600000)]; // Interval = 2 months

        expect(formatDateTimeForTimeAxis(1577865600000, mockScale)).toEqual(
            "1577865600000date"
        );

        mockScaleDomain = [new Date(1577865600000), new Date(1578124800000)]; // 2 months >= interval > 2 days

        expect(formatDateTimeForTimeAxis(1577865600000, mockScale)).toEqual(
            "1577865600000date"
        );
    });

    it("formatDateTimeForTimeAxis should return appropriate string for date and scale domain interval of less than or equal to 2 days", () => {
        mockScaleDomain = [new Date(1577865600000), new Date(1578038400000)]; // Interval = 2 days

        expect(formatDateTimeForTimeAxis(1577865600000, mockScale)).toEqual(
            "1577865600000date@1577865600000time"
        );

        mockScaleDomain = [new Date(1577865600000), new Date(1577952000000)]; // Interval < 2 days

        expect(formatDateTimeForTimeAxis(1577865600000, mockScale)).toEqual(
            "1577865600000date@1577865600000time"
        );
    });

    it("getTickValuesForTimeAxis should return empty array for invalid scale", () => {
        expect(
            getTickValuesForTimeAxis(
                null as unknown as ScaleTime<number, number>
            )
        ).toEqual([]);
    });

    it("getTickValuesForTimeAxis should return appropriate tick values for scale and default number of ticks", () => {
        mockScaleDomain = [new Date(1577865600000), new Date(1672560000000)];
        const timeTickValues = getTickValuesForTimeAxis(mockScale);

        expect(timeTickValues).toHaveLength(8);
        expect(timeTickValues[0]).toEqual(1577865600000);
        expect(timeTickValues[1]).toEqual(1591393371428.5715);
        expect(timeTickValues[2]).toEqual(1604921142857.1428);
        expect(timeTickValues[3]).toEqual(1618448914285.7144);
        expect(timeTickValues[4]).toEqual(1631976685714.2856);
        expect(timeTickValues[5]).toEqual(1645504457142.8572);
        expect(timeTickValues[6]).toEqual(1659032228571.4285);
        expect(timeTickValues[7]).toEqual(1672560000000);
    });

    it("getTickValuesForTimeAxis should return appropriate tick values for scale and invalid number of ticks", () => {
        mockScaleDomain = [new Date(1577865600000), new Date(1672560000000)];
        const timeTickValues = getTickValuesForTimeAxis(
            mockScale,
            null as unknown as number
        );

        expect(timeTickValues).toHaveLength(8);
        expect(timeTickValues[0]).toEqual(1577865600000);
        expect(timeTickValues[1]).toEqual(1591393371428.5715);
        expect(timeTickValues[2]).toEqual(1604921142857.1428);
        expect(timeTickValues[3]).toEqual(1618448914285.7144);
        expect(timeTickValues[4]).toEqual(1631976685714.2856);
        expect(timeTickValues[5]).toEqual(1645504457142.8572);
        expect(timeTickValues[6]).toEqual(1659032228571.4285);
        expect(timeTickValues[7]).toEqual(1672560000000);
    });

    it("getTickValuesForTimeAxis should return appropriate tick values for scale and number of ticks less than 3", () => {
        mockScaleDomain = [new Date(1577865600000), new Date(1672560000000)];
        const timeTickValues = getTickValuesForTimeAxis(mockScale, 1);

        expect(timeTickValues).toHaveLength(2);
        expect(timeTickValues[0]).toEqual(1577865600000);
        expect(timeTickValues[1]).toEqual(1672560000000);
    });

    it("getTickValuesForTimeAxis should return appropriate tick values for scale and number of ticks", () => {
        mockScaleDomain = [new Date(1577865600000), new Date(1672560000000)];
        const timeTickValues = getTickValuesForTimeAxis(mockScale, 3);

        expect(timeTickValues).toHaveLength(3);
        expect(timeTickValues[0]).toEqual(1577865600000);
        expect(timeTickValues[1]).toEqual(1625212800000);
        expect(timeTickValues[2]).toEqual(1672560000000);
    });

    it("getTickValuesForTimeAxis should return appropriate tick values for scale domain interval of 0", () => {
        mockScaleDomain = [new Date(1577865600000), new Date(1577865600000)];
        const timeTickValues = getTickValuesForTimeAxis(mockScale);

        expect(timeTickValues).toHaveLength(2);
        expect(timeTickValues[0]).toEqual(1577865600000);
        expect(timeTickValues[1]).toEqual(1577865600000);
    });

    it("checkIfOtherDimension should return false if undefined or does not equal other", () => {
        expect(checkIfOtherDimension(undefined)).toEqual(false);
        expect(checkIfOtherDimension("not other")).toEqual(false);
    });

    it("checkIfOtherDimension should return true equal to other", () => {
        expect(checkIfOtherDimension("other")).toEqual(true);
    });

    it("determineGranularity should return correct number", () => {
        // For empty
        expect(determineGranularity([])).toEqual(DAY_IN_MILLISECONDS);

        // For array length less than 3
        expect(determineGranularity([5, 10])).toEqual(5);

        // For length less than 3
        expect(determineGranularity([1, 2, 3, 4, 5, 6])).toEqual(1);

        // This should never be the case but for when the first diff is not the case
        expect(determineGranularity([1, 2, 4, 6, 8, 10])).toEqual(2);

        // This should never be the case but for when the first diff is not the case
        expect(determineGranularity([1, 2, 4, 5, 6, 7])).toEqual(1);
    });
});

let mockScaleDomain: Date[] = [];

const mockScale = {
    domain: jest.fn().mockImplementation(() => mockScaleDomain),
} as unknown as ScaleTime<number, number>;
