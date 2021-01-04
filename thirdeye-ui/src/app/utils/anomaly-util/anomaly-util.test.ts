import i18n from "i18next";
import { AnomalyCardData } from "../../components/entity-card/anomaly-card/anomaly-card.interfaces";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import * as dateTimeUtil from "../date-time-util/date-time-util";
import * as numberUtil from "../number-util/number-util";
import {
    createAlertEvaluation,
    createEmptyAnomalyCardData,
    filterAnomalies,
    getAnomalyCardData,
    getAnomalyCardDatas,
    getAnomalyName,
} from "./anomaly-util";

jest.mock("i18next");
jest.mock("../date-time-util/date-time-util");
jest.mock("../number-util/number-util");

describe("Anomaly Util", () => {
    beforeAll(() => {
        i18n.t = jest.fn().mockImplementation((key: string): string => {
            return key;
        });

        jest.spyOn(dateTimeUtil, "formatDateAndTime").mockImplementation(
            (date: number): string => {
                return date.toString();
            }
        );
        jest.spyOn(dateTimeUtil, "formatDuration").mockImplementation(
            (startTime: number, endTime: number): string => {
                return `${startTime.toString()} ${endTime.toString()}`;
            }
        );

        jest.spyOn(numberUtil, "formatLargeNumber").mockImplementation(
            (num: number): string => {
                return num.toString();
            }
        );
        jest.spyOn(numberUtil, "formatPercentage").mockImplementation(
            (num: number): string => {
                return num.toString();
            }
        );
    });

    beforeEach(() => {
        jest.clearAllMocks();
    });

    afterAll(() => {
        jest.restoreAllMocks();
    });

    test("getAnomalyName shall return no data available marker for an invalid anomaly", () => {
        const name = getAnomalyName((null as unknown) as Anomaly);

        expect(name).toEqual("label.no-data-available-marker");
    });

    test("getAnomalyName shall return appropriate string for anomaly", () => {
        const name = getAnomalyName(mockAnomaly);

        expect(name).toEqual("label.anomaly label.anomaly-id");
        expect(i18n.t).toHaveBeenCalledWith("label.anomaly-id", { id: 1 });
    });

    test("createEmptyAnomalyCardData shall create appropriate anomaly card data", () => {
        const anomalyCardData = createEmptyAnomalyCardData();

        expect(anomalyCardData).toEqual(mockEmptyAnomalyCardData);
    });

    test("createAlertEvaluation shall create appropriate alert evaluation", () => {
        const alertEvaluation = createAlertEvaluation(1, 2, 3);

        expect(alertEvaluation.alert).toBeDefined();
        expect(alertEvaluation.alert.id).toEqual(1);
        expect(alertEvaluation.start).toEqual(2);
        expect(alertEvaluation.end).toEqual(3);
    });

    test("getAnomalyCardData shall return empty anomaly card data for invalid anomaly", () => {
        const anomalyCardData = getAnomalyCardData(
            (null as unknown) as Anomaly
        );

        expect(anomalyCardData).toEqual(mockEmptyAnomalyCardData);
    });

    test("getAnomalyCardData shall return appropriate anomaly card data for anomaly", () => {
        const anomalyCardData = getAnomalyCardData(mockAnomaly1);

        expect(anomalyCardData).toEqual(mockAnomalyCardData1);
    });

    test("getAnomalyCardDatas shall return empty array for invalid anomalies", () => {
        const anomalyCardDatas = getAnomalyCardDatas(
            (null as unknown) as Anomaly[]
        );

        expect(anomalyCardDatas).toEqual([]);
    });

    test("getAnomalyCardDatas shall return empty array for empty anomalies", () => {
        const anomalyCardDatas = getAnomalyCardDatas([]);

        expect(anomalyCardDatas).toEqual([]);
    });

    test("getAnomalyCardDatas shall return appropriate anomaly card data array for anomalies", () => {
        const anomalyCardDatas = getAnomalyCardDatas(mockAnomalies);

        expect(anomalyCardDatas).toEqual(mockAnomalyCardDatas);
    });

    test("filterAnomalies shall return empty array for invalid anomaly card data array", () => {
        const anomalies = filterAnomalies(
            (null as unknown) as AnomalyCardData[],
            mockSearchWords
        );

        expect(anomalies).toEqual([]);
    });

    test("filterAnomalies shall return empty array for empty anomaly card data array", () => {
        const anomalies = filterAnomalies([], mockSearchWords);

        expect(anomalies).toEqual([]);
    });

    test("filterAnomalies shall return appropriate anomaly card data array for anomaly card data array when search words are invalid", () => {
        const anomalies = filterAnomalies(
            mockAnomalyCardDatas,
            (null as unknown) as string[]
        );

        expect(anomalies).toEqual(mockAnomalyCardDatas);
    });

    test("filterAnomalies shall return appropriate anomaly card data array for anomaly card data array when search words are empty", () => {
        const anomalies = filterAnomalies(mockAnomalyCardDatas, []);

        expect(anomalies).toEqual(mockAnomalyCardDatas);
    });

    test("filterAnomalies shall return appropriate anomaly card data array for anomaly card data array and search words", () => {
        const anomalies = filterAnomalies(
            mockAnomalyCardDatas,
            mockSearchWords
        );

        expect(anomalies).toHaveLength(1);
        expect(anomalies[0]).toEqual(mockAnomalyCardData1);
    });
});

const mockAnomaly = {
    id: 1,
} as Anomaly;

const mockEmptyAnomalyCardData: AnomalyCardData = {
    id: -1,
    name: "label.no-data-available-marker",
    alertName: "label.no-data-available-marker",
    alertId: -1,
    current: "label.no-data-available-marker",
    predicted: "label.no-data-available-marker",
    deviation: "label.no-data-available-marker",
    negativeDeviation: false,
    duration: "label.no-data-available-marker",
    startTime: "label.no-data-available-marker",
    endTime: "label.no-data-available-marker",
};

const mockAnomaly1: Anomaly = {
    id: 2,
    startTime: 3,
    endTime: 4,
    avgCurrentVal: 5,
    avgBaselineVal: 6,
    alert: {
        id: 7,
        name: "testAlertName7",
    },
} as Anomaly;

const mockAnomaly2: Anomaly = {
    id: 8,
} as Anomaly;

const mockAnomalies = [mockAnomaly1, mockAnomaly2];

const mockAnomalyCardData1: AnomalyCardData = {
    id: 2,
    name: "label.anomaly label.anomaly-id",
    alertName: "testAlertName7",
    alertId: 7,
    current: "5",
    predicted: "6",
    deviation: "-0.16666666666666666",
    negativeDeviation: true,
    duration: "3 4",
    startTime: "3",
    endTime: "4",
};

const mockAnomalyCardData2: AnomalyCardData = {
    id: 8,
    name: "label.anomaly label.anomaly-id",
    alertName: "label.no-data-available-marker",
    alertId: -1,
    current: "label.no-data-available-marker",
    predicted: "label.no-data-available-marker",
    deviation: "label.no-data-available-marker",
    negativeDeviation: false,
    duration: "label.no-data-available-marker",
    startTime: "label.no-data-available-marker",
    endTime: "label.no-data-available-marker",
};

const mockAnomalyCardDatas = [mockAnomalyCardData1, mockAnomalyCardData2];

const mockSearchWords = ["name6", "name7"];
