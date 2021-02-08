import i18n from "i18next";
import { AnomalyCardData } from "../../components/entity-cards/anomaly-card/anomaly-card.interfaces";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import {
    createAlertEvaluation,
    createEmptyAnomalyCardData,
    filterAnomalies,
    getAnomalyCardData,
    getAnomalyCardDatas,
    getAnomalyName,
} from "./anomalies.util";

jest.mock("i18next", () => ({
    t: jest.fn().mockImplementation((key) => key),
}));

jest.mock("../date-time/date-time.util", () => ({
    formatDateAndTime: jest.fn().mockImplementation((date) => date.toString()),
    formatDuration: jest
        .fn()
        .mockImplementation(
            (startTime, endTime) =>
                `${startTime.toString()} ${endTime.toString()}`
        ),
}));

jest.mock("../number/number.util", () => ({
    formatLargeNumber: jest.fn().mockImplementation((num) => num.toString()),
    formatPercentage: jest.fn().mockImplementation((num) => num.toString()),
}));

describe("Anomalies Util", () => {
    test("getAnomalyName should return no data available marker for invalid anomaly", () => {
        expect(getAnomalyName((null as unknown) as Anomaly)).toEqual(
            "label.no-data-marker"
        );
    });

    test("getAnomalyName should return appropriate name for anomaly", () => {
        expect(getAnomalyName(mockAnomaly)).toEqual(
            "label.anomaly label.anomaly-id"
        );
        expect(i18n.t).toHaveBeenCalledWith("label.anomaly-id", {
            id: 1,
        });
    });

    test("createEmptyAnomalyCardData should create appropriate anomaly card data", () => {
        expect(createEmptyAnomalyCardData()).toEqual(mockEmptyAnomalyCardData);
    });

    test("createAlertEvaluation should create appropriate alert evaluation", () => {
        expect(createAlertEvaluation(1, 2, 3)).toEqual({
            alert: {
                id: 1,
            },
            start: 2,
            end: 3,
        });
    });

    test("getAnomalyCardData should return empty anomaly card data for invalid anomaly", () => {
        expect(getAnomalyCardData((null as unknown) as Anomaly)).toEqual(
            mockEmptyAnomalyCardData
        );
    });

    test("getAnomalyCardData should return appropriate anomaly card data for anomaly", () => {
        expect(getAnomalyCardData(mockAnomaly1)).toEqual(mockAnomalyCardData1);
    });

    test("getAnomalyCardDatas should return empty array for invalid anomalies", () => {
        expect(getAnomalyCardDatas((null as unknown) as Anomaly[])).toEqual([]);
    });

    test("getAnomalyCardDatas should return empty array for empty anomalies", () => {
        expect(getAnomalyCardDatas([])).toEqual([]);
    });

    test("getAnomalyCardDatas should return appropriate anomaly card data array for anomalies", () => {
        expect(getAnomalyCardDatas(mockAnomalies)).toEqual(
            mockAnomalyCardDatas
        );
    });

    test("filterAnomalies should return empty array for invalid anomaly card data array", () => {
        expect(
            filterAnomalies(
                (null as unknown) as AnomalyCardData[],
                mockSearchWords
            )
        ).toEqual([]);
    });

    test("filterAnomalies should return empty array for empty anomaly card data array", () => {
        expect(filterAnomalies([], mockSearchWords)).toEqual([]);
    });

    test("filterAnomalies should return appropriate anomaly card data array for anomaly card data array and invalid search words", () => {
        expect(
            filterAnomalies(mockAnomalyCardDatas, (null as unknown) as string[])
        ).toEqual(mockAnomalyCardDatas);
    });

    test("filterAnomalies should return appropriate anomaly card data array for anomaly card data array and empty search words", () => {
        expect(filterAnomalies(mockAnomalyCardDatas, [])).toEqual(
            mockAnomalyCardDatas
        );
    });

    test("filterAnomalies should return appropriate anomaly card data array for anomaly card data array and search words", () => {
        expect(filterAnomalies(mockAnomalyCardDatas, mockSearchWords)).toEqual([
            mockAnomalyCardData1,
        ]);
    });
});

const mockAnomaly = {
    id: 1,
} as Anomaly;

const mockEmptyAnomalyCardData = {
    id: -1,
    name: "label.no-data-marker",
    alertName: "label.no-data-marker",
    alertId: -1,
    current: "label.no-data-marker",
    predicted: "label.no-data-marker",
    deviation: "label.no-data-marker",
    negativeDeviation: false,
    duration: "label.no-data-marker",
    startTime: "label.no-data-marker",
    endTime: "label.no-data-marker",
};

const mockAnomaly1 = {
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

const mockAnomaly2 = {
    id: 8,
} as Anomaly;

const mockAnomalies = [mockAnomaly1, mockAnomaly2];

const mockAnomalyCardData1 = {
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

const mockAnomalyCardData2 = {
    id: 8,
    name: "label.anomaly label.anomaly-id",
    alertName: "label.no-data-marker",
    alertId: -1,
    current: "label.no-data-marker",
    predicted: "label.no-data-marker",
    deviation: "label.no-data-marker",
    negativeDeviation: false,
    duration: "label.no-data-marker",
    startTime: "label.no-data-marker",
    endTime: "label.no-data-marker",
};

const mockAnomalyCardDatas = [mockAnomalyCardData1, mockAnomalyCardData2];

const mockSearchWords = ["name6", "name7"];
