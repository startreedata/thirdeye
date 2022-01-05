import { ScaleTime } from "d3-scale";
import { cloneDeep } from "lodash";
import { Settings } from "luxon";
import { AlertEvaluationTimeSeriesPoint } from "../../components/visualizations/alert-evaluation-time-series/alert-evaluation-time-series/alert-evaluation-time-series.interfaces";
import { Alert, AlertEvaluation } from "../../rest/dto/alert.interfaces";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import {
    DetectionData,
    DetectionEvaluation,
} from "../../rest/dto/detection.interfaces";
import {
    filterAlertEvaluationTimeSeriesPointsByTime,
    formatDateTimeForTimeAxis,
    formatLargeNumberForVisualization,
    getAlertEvaluationAnomalies,
    getAlertEvaluationTimeSeriesPointAtTime,
    getAlertEvaluationTimeSeriesPoints,
    getAlertEvaluationTimeSeriesPointsMaxTimestamp,
    getAlertEvaluationTimeSeriesPointsMaxValue,
    getAlertEvaluationTimeSeriesPointsMinTimestamp,
    getTickValuesForTimeAxis,
} from "./visualization.util";

const systemLocale = Settings.defaultLocale;
const systemZoneName = Settings.defaultZoneName;

jest.mock("../number/number.util", () => ({
    formatLargeNumber: jest.fn().mockImplementation((num) => num.toString()),
}));

jest.mock("../date-time/date-time.util", () => ({
    formatYear: jest
        .fn()
        .mockImplementation((date) => `${date.toString()}year`),
    formatMonthOfYear: jest
        .fn()
        .mockImplementation((date) => `${date.toString()}monthOfYear`),
    formatDate: jest
        .fn()
        .mockImplementation((date) => `${date.toString()}date`),
    formatTime: jest
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
            formatLargeNumberForVisualization((null as unknown) as number)
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
            formatDateTimeForTimeAxis((null as unknown) as number, mockScale)
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
                (null as unknown) as ScaleTime<number, number>
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
                (null as unknown) as ScaleTime<number, number>
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
            (null as unknown) as number
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

    it("getAlertEvaluationTimeSeriesPoints should return empty array for invalid alert evaluation", () => {
        expect(
            getAlertEvaluationTimeSeriesPoints(
                (null as unknown) as AlertEvaluation
            )
        ).toEqual([]);
    });

    it("getAlertEvaluationTimeSeriesPoints should return empty array for invalid detection evaluations", () => {
        const mockAlertEvaluationCopy = cloneDeep(mockAlertEvaluation);
        mockAlertEvaluationCopy.detectionEvaluations = (null as unknown) as {
            [index: string]: DetectionEvaluation;
        };

        expect(
            getAlertEvaluationTimeSeriesPoints(mockAlertEvaluationCopy)
        ).toEqual([]);
    });

    it("getAlertEvaluationTimeSeriesPoints should return empty array for empty detection evaluations", () => {
        const mockAlertEvaluationCopy = cloneDeep(mockAlertEvaluation);
        mockAlertEvaluationCopy.detectionEvaluations = {};

        expect(
            getAlertEvaluationTimeSeriesPoints(mockAlertEvaluationCopy)
        ).toEqual([]);
    });

    it("getAlertEvaluationTimeSeriesPoints should return empty array for invalid data in detection evaluation", () => {
        const mockAlertEvaluationCopy = cloneDeep(mockAlertEvaluation);
        mockAlertEvaluationCopy.detectionEvaluations.detectionEvaluation1 = {
            data: (null as unknown) as DetectionData,
        } as DetectionEvaluation;

        expect(
            getAlertEvaluationTimeSeriesPoints(mockAlertEvaluationCopy)
        ).toEqual([]);
    });

    it("getAlertEvaluationTimeSeriesPoints should return empty array for empty data in detection evaluation", () => {
        const mockAlertEvaluationCopy = cloneDeep(mockAlertEvaluation);
        mockAlertEvaluationCopy.detectionEvaluations.detectionEvaluation1 = {
            data: {},
        } as DetectionEvaluation;

        expect(
            getAlertEvaluationTimeSeriesPoints(mockAlertEvaluationCopy)
        ).toEqual([]);
    });

    it("getAlertEvaluationTimeSeriesPoints should return empty array for invalid timestamps in detection evaluation", () => {
        const mockAlertEvaluationCopy = cloneDeep(mockAlertEvaluation);
        mockAlertEvaluationCopy.detectionEvaluations.detectionEvaluation1 = {
            data: {
                timestamp: (null as unknown) as number[],
            },
        } as DetectionEvaluation;

        expect(
            getAlertEvaluationTimeSeriesPoints(mockAlertEvaluationCopy)
        ).toEqual([]);
    });

    it("getAlertEvaluationTimeSeriesPoints should return empty array for empty timestamps in detection evaluation", () => {
        const mockAlertEvaluationCopy = cloneDeep(mockAlertEvaluation);
        mockAlertEvaluationCopy.detectionEvaluations.detectionEvaluation1 = {
            data: {
                timestamp: [] as number[],
            },
        } as DetectionEvaluation;

        expect(
            getAlertEvaluationTimeSeriesPoints(mockAlertEvaluationCopy)
        ).toEqual([]);
    });

    it("getAlertEvaluationTimeSeriesPoints should return appropriate alert evaluation time series points for alert evaluation", () => {
        expect(getAlertEvaluationTimeSeriesPoints(mockAlertEvaluation)).toEqual(
            mockAlertEvaluationTimeSeriesPoints
        );
    });

    it("getAlertEvaluationAnomalies should return empty array for invalid alert evaluation", () => {
        expect(
            getAlertEvaluationAnomalies((null as unknown) as AlertEvaluation)
        ).toEqual([]);
    });

    it("getAlertEvaluationAnomalies should return empty array for invalid detection evaluations", () => {
        const mockAlertEvaluationCopy = cloneDeep(mockAlertEvaluation);
        mockAlertEvaluationCopy.detectionEvaluations = (null as unknown) as {
            [index: string]: DetectionEvaluation;
        };

        expect(getAlertEvaluationAnomalies(mockAlertEvaluationCopy)).toEqual(
            []
        );
    });

    it("getAlertEvaluationAnomalies should return empty array for empty detection evaluations", () => {
        const mockAlertEvaluationCopy = cloneDeep(mockAlertEvaluation);
        mockAlertEvaluationCopy.detectionEvaluations = {};

        expect(getAlertEvaluationAnomalies(mockAlertEvaluationCopy)).toEqual(
            []
        );
    });

    it("getAlertEvaluationAnomalies should return empty array for invalid anomalies in detection evaluation", () => {
        const mockAlertEvaluationCopy = cloneDeep(mockAlertEvaluation);
        mockAlertEvaluationCopy.detectionEvaluations.detectionEvaluation1 = {
            anomalies: (null as unknown) as Anomaly[],
        } as DetectionEvaluation;

        expect(getAlertEvaluationAnomalies(mockAlertEvaluationCopy)).toEqual(
            []
        );
    });

    it("getAlertEvaluationAnomalies should return empty array for empty anomalies in detection evaluation", () => {
        const mockAlertEvaluationCopy = cloneDeep(mockAlertEvaluation);
        mockAlertEvaluationCopy.detectionEvaluations.detectionEvaluation1 = {
            anomalies: [] as Anomaly[],
        } as DetectionEvaluation;

        expect(getAlertEvaluationAnomalies(mockAlertEvaluationCopy)).toEqual(
            []
        );
    });

    it("getAlertEvaluationAnomalies should return appropriate alert evaluation anomalies for alert evaluation", () => {
        expect(getAlertEvaluationAnomalies(mockAlertEvaluation)).toEqual(
            mockAnomalies
        );
    });

    it("getAlertEvaluationTimeSeriesPointsMinTimestamp should return 0 for invalid alert evaluation time series points", () => {
        expect(
            getAlertEvaluationTimeSeriesPointsMinTimestamp(
                (null as unknown) as AlertEvaluationTimeSeriesPoint[]
            )
        ).toEqual(0);
    });

    it("getAlertEvaluationTimeSeriesPointsMinTimestamp should return 0 for empty alert evaluation time series points", () => {
        expect(getAlertEvaluationTimeSeriesPointsMinTimestamp([])).toEqual(0);
    });

    it("getAlertEvaluationTimeSeriesPointsMinTimestamp should return appropriate timestamp for alert evaluation time series points", () => {
        expect(
            getAlertEvaluationTimeSeriesPointsMinTimestamp(
                mockAlertEvaluationTimeSeriesPoints
            )
        ).toEqual(1);
    });

    it("getAlertEvaluationTimeSeriesPointsMaxTimestamp should return 0 for invalid alert evaluation time series points", () => {
        expect(
            getAlertEvaluationTimeSeriesPointsMaxTimestamp(
                (null as unknown) as AlertEvaluationTimeSeriesPoint[]
            )
        ).toEqual(0);
    });

    it("getAlertEvaluationTimeSeriesPointsMaxTimestamp should return 0 for empty alert evaluation time series points", () => {
        expect(getAlertEvaluationTimeSeriesPointsMaxTimestamp([])).toEqual(0);
    });

    it("getAlertEvaluationTimeSeriesPointsMaxTimestamp should return appropriate timestamp for alert evaluation time series points", () => {
        expect(
            getAlertEvaluationTimeSeriesPointsMaxTimestamp(
                mockAlertEvaluationTimeSeriesPoints
            )
        ).toEqual(3);
    });

    it("getAlertEvaluationTimeSeriesPointsMaxValue should return 0 for invalid alert evaluation time series points", () => {
        expect(
            getAlertEvaluationTimeSeriesPointsMaxValue(
                (null as unknown) as AlertEvaluationTimeSeriesPoint[]
            )
        ).toEqual(0);
    });

    it("getAlertEvaluationTimeSeriesPointsMaxValue should return 0 for empty alert evaluation time series points", () => {
        expect(getAlertEvaluationTimeSeriesPointsMaxValue([])).toEqual(0);
    });

    it("getAlertEvaluationTimeSeriesPointsMaxValue should return appropriate value for alert evaluation time series points", () => {
        expect(
            getAlertEvaluationTimeSeriesPointsMaxValue(
                mockAlertEvaluationTimeSeriesPoints
            )
        ).toEqual(15);

        const mockAlertEvaluationTimeSeriesPointsCopy = cloneDeep(
            mockAlertEvaluationTimeSeriesPoints
        );
        mockAlertEvaluationTimeSeriesPointsCopy[0].current = NaN;
        mockAlertEvaluationTimeSeriesPointsCopy[0].expected = NaN;
        mockAlertEvaluationTimeSeriesPointsCopy[1].lowerBound = NaN;
        mockAlertEvaluationTimeSeriesPointsCopy[2].upperBound = NaN;

        expect(
            getAlertEvaluationTimeSeriesPointsMaxValue(
                mockAlertEvaluationTimeSeriesPointsCopy
            )
        ).toEqual(15);
    });

    it("filterAlertEvaluationTimeSeriesPointsByTime should return empty array for invalid alert evaluation time series points", () => {
        expect(
            filterAlertEvaluationTimeSeriesPointsByTime(
                (null as unknown) as AlertEvaluationTimeSeriesPoint[],
                1,
                2
            )
        ).toEqual([]);
    });

    it("filterAlertEvaluationTimeSeriesPointsByTime should return empty array for empty alert evaluation time series points", () => {
        expect(filterAlertEvaluationTimeSeriesPointsByTime([], 1, 2)).toEqual(
            []
        );
    });

    it("filterAlertEvaluationTimeSeriesPointsByTime should return appropriate alert evaluation time series points for alert evaluation time series points and invalid start and end time", () => {
        expect(
            filterAlertEvaluationTimeSeriesPointsByTime(
                mockAlertEvaluationTimeSeriesPoints,
                (null as unknown) as number,
                1
            )
        ).toEqual(mockAlertEvaluationTimeSeriesPoints);
        expect(
            filterAlertEvaluationTimeSeriesPointsByTime(
                mockAlertEvaluationTimeSeriesPoints,
                1,
                (null as unknown) as number
            )
        ).toEqual(mockAlertEvaluationTimeSeriesPoints);
        expect(
            filterAlertEvaluationTimeSeriesPointsByTime(
                mockAlertEvaluationTimeSeriesPoints,
                (null as unknown) as number,
                (null as unknown) as number
            )
        ).toEqual(mockAlertEvaluationTimeSeriesPoints);
    });

    it("filterAlertEvaluationTimeSeriesPointsByTime should return appropriate alert evaluation time series points for alert evaluation time series points and start and end time", () => {
        expect(
            filterAlertEvaluationTimeSeriesPointsByTime(
                mockAlertEvaluationTimeSeriesPoints,
                2,
                2
            )
        ).toEqual([mockAlertEvaluationTimeSeriesPoint2]);
        expect(
            filterAlertEvaluationTimeSeriesPointsByTime(
                mockAlertEvaluationTimeSeriesPoints,
                1,
                3
            )
        ).toEqual(mockAlertEvaluationTimeSeriesPoints);
        expect(
            filterAlertEvaluationTimeSeriesPointsByTime(
                mockAlertEvaluationTimeSeriesPoints,
                0,
                4
            )
        ).toEqual(mockAlertEvaluationTimeSeriesPoints);
        expect(
            filterAlertEvaluationTimeSeriesPointsByTime(
                mockAlertEvaluationTimeSeriesPoints,
                -1,
                2
            )
        ).toEqual([
            mockAlertEvaluationTimeSeriesPoint1,
            mockAlertEvaluationTimeSeriesPoint2,
        ]);
        expect(
            filterAlertEvaluationTimeSeriesPointsByTime(
                mockAlertEvaluationTimeSeriesPoints,
                2,
                5
            )
        ).toEqual([
            mockAlertEvaluationTimeSeriesPoint2,
            mockAlertEvaluationTimeSeriesPoint3,
        ]);
        expect(
            filterAlertEvaluationTimeSeriesPointsByTime(
                mockAlertEvaluationTimeSeriesPoints,
                -1,
                0
            )
        ).toEqual([]);
        expect(
            filterAlertEvaluationTimeSeriesPointsByTime(
                mockAlertEvaluationTimeSeriesPoints,
                4,
                5
            )
        ).toEqual([]);
    });

    it("getAlertEvaluationTimeSeriesPointAtTime should return null for invalid alert evaluation time series points", () => {
        expect(
            getAlertEvaluationTimeSeriesPointAtTime(
                (null as unknown) as AlertEvaluationTimeSeriesPoint[],
                1
            )
        ).toBeNull();
    });

    it("getAlertEvaluationTimeSeriesPointAtTime should return null for empty alert evaluation time series points", () => {
        expect(getAlertEvaluationTimeSeriesPointAtTime([], 1)).toBeNull();
    });

    it("getAlertEvaluationTimeSeriesPointAtTime should return null for alert evaluation time series points and invalid time", () => {
        expect(
            getAlertEvaluationTimeSeriesPointAtTime(
                mockAlertEvaluationTimeSeriesPoints,
                (null as unknown) as number
            )
        ).toBeNull();
    });

    it("getAlertEvaluationTimeSeriesPointAtTime should return appropriate alert evaluation time series point for alert evaluation time series points and time", () => {
        expect(
            getAlertEvaluationTimeSeriesPointAtTime(
                mockAlertEvaluationTimeSeriesPoints,
                2
            )
        ).toEqual(mockAlertEvaluationTimeSeriesPoint2);
        expect(
            getAlertEvaluationTimeSeriesPointAtTime(
                mockAlertEvaluationTimeSeriesPoints,
                1
            )
        ).toEqual(mockAlertEvaluationTimeSeriesPoint1);
        expect(
            getAlertEvaluationTimeSeriesPointAtTime(
                mockAlertEvaluationTimeSeriesPoints,
                3
            )
        ).toEqual(mockAlertEvaluationTimeSeriesPoint3);
        expect(
            getAlertEvaluationTimeSeriesPointAtTime(
                mockAlertEvaluationTimeSeriesPoints,
                0
            )
        ).toBeNull();
        expect(
            getAlertEvaluationTimeSeriesPointAtTime(
                mockAlertEvaluationTimeSeriesPoints,
                4
            )
        ).toEqual(mockAlertEvaluationTimeSeriesPoint3);
    });
});

let mockScaleDomain: Date[] = [];

const mockScale = ({
    domain: jest.fn().mockImplementation(() => mockScaleDomain),
} as unknown) as ScaleTime<number, number>;

const mockAlertEvaluation = {
    alert: {} as Alert,
    detectionEvaluations: {
        detectionEvaluation1: {
            data: {
                timestamp: [1, 2, 3],
                upperBound: [4, 5, 6],
                lowerBound: [7, 8, 9],
                current: [10, 11, 12],
                expected: [13, 14, 15],
            },
            anomalies: [
                {
                    startTime: 16,
                    endTime: 17,
                    avgCurrentVal: 18,
                    avgBaselineVal: 19,
                },
                {
                    startTime: 20,
                    endTime: 21,
                    avgCurrentVal: 22,
                    avgBaselineVal: 23,
                },
            ],
        } as DetectionEvaluation,
        detectionEvaluation2: {
            data: {
                timestamp: [24],
                upperBound: [25],
                lowerBound: [26],
                current: [27],
                expected: [28],
            },
            anomalies: [
                {
                    startTime: 29,
                    endTime: 30,
                    avgCurrentVal: 31,
                    avgBaselineVal: 32,
                },
                {
                    startTime: 33,
                    endTime: 34,
                    avgCurrentVal: 35,
                    avgBaselineVal: 36,
                },
            ],
        } as DetectionEvaluation,
    },
    start: 37,
    end: 38,
    lastTimestamp: 39,
} as AlertEvaluation;

const mockAlertEvaluationTimeSeriesPoint1 = {
    timestamp: 1,
    upperBound: 4,
    lowerBound: 7,
    current: 10,
    expected: 13,
};

const mockAlertEvaluationTimeSeriesPoint2 = {
    timestamp: 2,
    upperBound: 5,
    lowerBound: 8,
    current: 11,
    expected: 14,
};

const mockAlertEvaluationTimeSeriesPoint3 = {
    timestamp: 3,
    upperBound: 6,
    lowerBound: 9,
    current: 12,
    expected: 15,
};

const mockAlertEvaluationTimeSeriesPoints = [
    mockAlertEvaluationTimeSeriesPoint1,
    mockAlertEvaluationTimeSeriesPoint2,
    mockAlertEvaluationTimeSeriesPoint3,
];

const mockAnomaly1 = {
    startTime: 16,
    endTime: 17,
    avgCurrentVal: 18,
    avgBaselineVal: 19,
};

const mockAnomaly2 = {
    startTime: 20,
    endTime: 21,
    avgCurrentVal: 22,
    avgBaselineVal: 23,
};

const mockAnomalies = [mockAnomaly1, mockAnomaly2];
