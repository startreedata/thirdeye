import { cloneDeep } from "lodash";
import {
    AlertEvaluationAnomalyPoint,
    AlertEvaluationTimeSeriesPoint,
} from "../../components/visualizations/alert-evaluation-time-series/alert-evaluation-time-series.interfaces";
import { Alert, AlertEvaluation } from "../../rest/dto/alert.interfaces";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import {
    DetectionData,
    DetectionEvaluation,
} from "../../rest/dto/detection.interfaces";
import {
    formatLargeNumberForVisualization,
    getAlertEvaluationAnomalyPoints,
    getAlertEvaluationTimeSeriesPoints,
    getAlertEvaluationTimeSeriesPointsMaxTimestamp,
    getAlertEvaluationTimeSeriesPointsMaxValue,
    getAlertEvaluationTimeSeriesPointsMinTimestamp,
} from "./visualization-util";

jest.mock("../number-util/number-util", () => ({
    formatLargeNumber: jest.fn().mockImplementation((num: number): string => {
        return num.toString();
    }),
}));

describe("Visualization Util", () => {
    test("formatLargeNumberForVisualization should return empty string for invalid number", () => {
        expect(
            formatLargeNumberForVisualization((null as unknown) as number)
        ).toEqual("");
    });

    test("formatLargeNumberForVisualization should return appropriate string for number", () => {
        expect(formatLargeNumberForVisualization(1)).toEqual("1");
    });

    test("formatLargeNumberForVisualization should return appropriate string for number object", () => {
        expect(
            formatLargeNumberForVisualization({
                valueOf: (): number => 1,
            })
        ).toEqual("1");
    });

    test("getAlertEvaluationTimeSeriesPoints should return empty alert evaluation time series points for invalid alert evaluation", () => {
        expect(
            getAlertEvaluationTimeSeriesPoints(
                (null as unknown) as AlertEvaluation
            )
        ).toEqual([]);
    });

    test("getAlertEvaluationTimeSeriesPoints should return empty alert evaluation time series points for invalid detection evaluation", () => {
        const mockAlertEvaluationCopy = cloneDeep(mockAlertEvaluation);
        mockAlertEvaluationCopy.detectionEvaluations = (null as unknown) as {
            [index: string]: DetectionEvaluation;
        };

        expect(
            getAlertEvaluationTimeSeriesPoints(mockAlertEvaluationCopy)
        ).toEqual([]);
    });

    test("getAlertEvaluationTimeSeriesPoints should return empty alert evaluation time series points for empty detection evaluation", () => {
        const mockAlertEvaluationCopy = cloneDeep(mockAlertEvaluation);
        mockAlertEvaluationCopy.detectionEvaluations = {};

        expect(
            getAlertEvaluationTimeSeriesPoints(mockAlertEvaluationCopy)
        ).toEqual([]);
    });

    test("getAlertEvaluationTimeSeriesPoints should return empty alert evaluation time series points for invalid data in detection evaluation", () => {
        const mockAlertEvaluationCopy = cloneDeep(mockAlertEvaluation);
        mockAlertEvaluationCopy.detectionEvaluations.detectionEvaluation1 = {
            data: (null as unknown) as DetectionData,
        } as DetectionEvaluation;

        expect(
            getAlertEvaluationTimeSeriesPoints(mockAlertEvaluationCopy)
        ).toEqual([]);
    });

    test("getAlertEvaluationTimeSeriesPoints should return empty alert evaluation time series points for empty data in detection evaluation", () => {
        const mockAlertEvaluationCopy = cloneDeep(mockAlertEvaluation);
        mockAlertEvaluationCopy.detectionEvaluations.detectionEvaluation1 = {
            data: {},
        } as DetectionEvaluation;

        expect(
            getAlertEvaluationTimeSeriesPoints(mockAlertEvaluationCopy)
        ).toEqual([]);
    });

    test("getAlertEvaluationTimeSeriesPoints should return empty alert evaluation time series points for invalid timestamps in detection evaluation", () => {
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

    test("getAlertEvaluationTimeSeriesPoints should return empty alert evaluation time series points for empty timestamps in detection evaluation", () => {
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

    test("getAlertEvaluationTimeSeriesPoints should return appropriate alert evaluation time series points for alert evaluation", () => {
        expect(getAlertEvaluationTimeSeriesPoints(mockAlertEvaluation)).toEqual(
            mockAlertEvaluationTimeSeriesPoints
        );
    });

    test("getAlertEvaluationAnomalyPoints should return empty alert evaluation anomaly points for invalid alert evaluation", () => {
        expect(
            getAlertEvaluationAnomalyPoints(
                (null as unknown) as AlertEvaluation
            )
        ).toEqual([]);
    });

    test("getAlertEvaluationAnomalyPoints should return empty alert evaluation anomaly points for invalid detection evaluation", () => {
        const mockAlertEvaluationCopy = cloneDeep(mockAlertEvaluation);
        mockAlertEvaluationCopy.detectionEvaluations = (null as unknown) as {
            [index: string]: DetectionEvaluation;
        };

        expect(
            getAlertEvaluationAnomalyPoints(mockAlertEvaluationCopy)
        ).toEqual([]);
    });

    test("getAlertEvaluationAnomalyPoints should return empty alert evaluation anonmaly points for empty detection evaluation", () => {
        const mockAlertEvaluationCopy = cloneDeep(mockAlertEvaluation);
        mockAlertEvaluationCopy.detectionEvaluations = {};

        expect(
            getAlertEvaluationAnomalyPoints(mockAlertEvaluationCopy)
        ).toEqual([]);
    });

    test("getAlertEvaluationAnomalyPoints should return empty alert evaluation anomaly points for invalid anomalies in detection evaluation", () => {
        const mockAlertEvaluationCopy = cloneDeep(mockAlertEvaluation);
        mockAlertEvaluationCopy.detectionEvaluations.detectionEvaluation1 = {
            anomalies: (null as unknown) as Anomaly[],
        } as DetectionEvaluation;

        expect(
            getAlertEvaluationAnomalyPoints(mockAlertEvaluationCopy)
        ).toEqual([]);
    });

    test("getAlertEvaluationAnomalyPoints should return empty alert evaluation anomaly points for empty anomalies in detection evaluation", () => {
        const mockAlertEvaluationCopy = cloneDeep(mockAlertEvaluation);
        mockAlertEvaluationCopy.detectionEvaluations.detectionEvaluation1 = {
            anomalies: [] as Anomaly[],
        } as DetectionEvaluation;

        expect(
            getAlertEvaluationAnomalyPoints(mockAlertEvaluationCopy)
        ).toEqual([]);
    });

    test("getAlertEvaluationAnomalyPoints should return appropriate alert evaluation anomaly points for alert evaluation", () => {
        expect(getAlertEvaluationAnomalyPoints(mockAlertEvaluation)).toEqual(
            mockAlertEvaluationAnomalyPoints
        );
    });

    test("getAlertEvaluationTimeSeriesPointsMinTimestamp should return 0 for invalid alert evaluation time series points", () => {
        expect(
            getAlertEvaluationTimeSeriesPointsMinTimestamp(
                (null as unknown) as AlertEvaluationTimeSeriesPoint[]
            )
        ).toEqual(0);
    });

    test("getAlertEvaluationTimeSeriesPointsMinTimestamp should return 0 for empty alert evaluation time series points", () => {
        expect(getAlertEvaluationTimeSeriesPointsMinTimestamp([])).toEqual(0);
    });

    test("getAlertEvaluationTimeSeriesPointsMinTimestamp should return appropriate timestamp for alert evaluation time series points", () => {
        expect(
            getAlertEvaluationTimeSeriesPointsMinTimestamp(
                mockAlertEvaluationTimeSeriesPoints
            )
        ).toEqual(1);

        const mockAlertEvaluationTimeSeriesPointsCopy = cloneDeep(
            mockAlertEvaluationTimeSeriesPoints
        );
        mockAlertEvaluationTimeSeriesPointsCopy[0].timestamp = Number.MAX_VALUE;
        mockAlertEvaluationTimeSeriesPointsCopy[1].timestamp = Number.MIN_VALUE;

        expect(
            getAlertEvaluationTimeSeriesPointsMinTimestamp(
                mockAlertEvaluationTimeSeriesPointsCopy
            )
        ).toEqual(Number.MIN_VALUE);
    });

    test("getAlertEvaluationTimeSeriesPointsMaxTimestamp should return 0 for invalid alert evaluation time series points", () => {
        expect(
            getAlertEvaluationTimeSeriesPointsMaxTimestamp(
                (null as unknown) as AlertEvaluationTimeSeriesPoint[]
            )
        ).toEqual(0);
    });

    test("getAlertEvaluationTimeSeriesPointsMaxTimestamp should return 0 for empty alert evaluation time series points", () => {
        expect(getAlertEvaluationTimeSeriesPointsMaxTimestamp([])).toEqual(0);
    });

    test("getAlertEvaluationTimeSeriesPointsMaxTimestamp should return appropriate timestamp for alert evaluation time series points", () => {
        expect(
            getAlertEvaluationTimeSeriesPointsMaxTimestamp(
                mockAlertEvaluationTimeSeriesPoints
            )
        ).toEqual(3);

        const mockAlertEvaluationTimeSeriesPointsCopy = cloneDeep(
            mockAlertEvaluationTimeSeriesPoints
        );
        mockAlertEvaluationTimeSeriesPointsCopy[0].timestamp = Number.MAX_VALUE;
        mockAlertEvaluationTimeSeriesPointsCopy[1].timestamp = Number.MIN_VALUE;

        expect(
            getAlertEvaluationTimeSeriesPointsMaxTimestamp(
                mockAlertEvaluationTimeSeriesPointsCopy
            )
        ).toEqual(Number.MAX_VALUE);
    });

    test("getAlertEvaluationTimeSeriesPointsMaxValue should return 0 for invalid alert evaluation time series points", () => {
        expect(
            getAlertEvaluationTimeSeriesPointsMaxValue(
                (null as unknown) as AlertEvaluationTimeSeriesPoint[]
            )
        ).toEqual(0);
    });

    test("getAlertEvaluationTimeSeriesPointsMaxValue should return 0 for empty alert evaluation time series points", () => {
        expect(getAlertEvaluationTimeSeriesPointsMaxValue([])).toEqual(0);
    });

    test("getAlertEvaluationTimeSeriesPointsMaxValue should return appropriate value for alert evaluation time series points", () => {
        expect(
            getAlertEvaluationTimeSeriesPointsMaxValue(
                mockAlertEvaluationTimeSeriesPoints
            )
        ).toEqual(15);

        const mockAlertEvaluationTimeSeriesPointsCopy = cloneDeep(
            mockAlertEvaluationTimeSeriesPoints
        );
        mockAlertEvaluationTimeSeriesPointsCopy[0].current = NaN;
        mockAlertEvaluationTimeSeriesPointsCopy[1].lowerBound = NaN;
        mockAlertEvaluationTimeSeriesPointsCopy[2].upperBound = NaN;
        mockAlertEvaluationTimeSeriesPointsCopy[0].expected = NaN;

        expect(
            getAlertEvaluationTimeSeriesPointsMaxValue(
                mockAlertEvaluationTimeSeriesPointsCopy
            )
        ).toEqual(15);
    });
});

const mockAlertEvaluation: AlertEvaluation = {
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
};

const mockAlertEvaluationTimeSeriesPoints: AlertEvaluationTimeSeriesPoint[] = [
    {
        timestamp: 1,
        upperBound: 4,
        lowerBound: 7,
        current: 10,
        expected: 13,
    },
    {
        timestamp: 2,
        upperBound: 5,
        lowerBound: 8,
        current: 11,
        expected: 14,
    },
    {
        timestamp: 3,
        upperBound: 6,
        lowerBound: 9,
        current: 12,
        expected: 15,
    },
];

const mockAlertEvaluationAnomalyPoints: AlertEvaluationAnomalyPoint[] = [
    {
        startTime: 16,
        endTime: 17,
        current: 18,
        baseline: 19,
    },
    {
        startTime: 20,
        endTime: 21,
        current: 22,
        baseline: 23,
    },
];
