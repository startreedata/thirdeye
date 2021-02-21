import i18n from "i18next";
import { AnomalyCardData } from "../../components/entity-cards/anomaly-card/anomaly-card.interfaces";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import {
    createAlertEvaluation,
    createEmptyAnomalyCardData,
    filterAnomalies,
    filterAnomaliesByTime,
    getAnomaliesAtTime,
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
    test("getAnomalyName should return appropriate name for invalid anomaly", () => {
        expect(getAnomalyName((null as unknown) as Anomaly)).toEqual(
            "label.anomaly"
        );
    });

    test("getAnomalyName should return appropriate name for anomaly with invalid id", () => {
        expect(getAnomalyName({} as Anomaly)).toEqual("label.anomaly");
    });

    test("getAnomalyName should return appropriate name for anomaly", () => {
        expect(getAnomalyName(mockAnomaly1)).toEqual(
            "label.anomaly label.entity-id"
        );
        expect(i18n.t).toHaveBeenCalledWith("label.entity-id", { id: 1 });
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
            mockAnomalyCardData2,
        ]);
    });

    test("filterAnomaliesByTime should return empty array for invalid anomalies", () => {
        expect(
            filterAnomaliesByTime((null as unknown) as Anomaly[], 1, 2)
        ).toEqual([]);
    });

    test("filterAnomaliesByTime should return empty array for empty anomalies", () => {
        expect(filterAnomaliesByTime([], 1, 2)).toEqual([]);
    });

    test("filterAnomaliesByTime should return appropriate anomaly array for anomalies and invalid start and end time", () => {
        expect(
            filterAnomaliesByTime(mockAnomalies, (null as unknown) as number, 1)
        ).toEqual(mockAnomalies);
        expect(
            filterAnomaliesByTime(mockAnomalies, 1, (null as unknown) as number)
        ).toEqual(mockAnomalies);
        expect(
            filterAnomaliesByTime(
                mockAnomalies,
                (null as unknown) as number,
                (null as unknown) as number
            )
        ).toEqual(mockAnomalies);
    });

    test("filterAnomaliesByTime should return appropriate anomaly array for anomalies and start and end time", () => {
        expect(filterAnomaliesByTime(mockAnomalies, 2, 2)).toEqual([
            mockAnomaly1,
        ]);
        expect(filterAnomaliesByTime(mockAnomalies, 2, 3)).toEqual([
            mockAnomaly1,
            mockAnomaly3,
        ]);
        expect(filterAnomaliesByTime(mockAnomalies, 3, 3)).toEqual([
            mockAnomaly1,
            mockAnomaly3,
        ]);
        expect(filterAnomaliesByTime(mockAnomalies, 2, 9)).toEqual([
            mockAnomaly1,
            mockAnomaly3,
            mockAnomaly2,
        ]);
        expect(filterAnomaliesByTime(mockAnomalies, 1, 10)).toEqual([
            mockAnomaly1,
            mockAnomaly3,
            mockAnomaly2,
        ]);
        expect(filterAnomaliesByTime(mockAnomalies, 1, 2)).toEqual([
            mockAnomaly1,
        ]);
        expect(filterAnomaliesByTime(mockAnomalies, 9, 10)).toEqual([
            mockAnomaly2,
        ]);
        expect(filterAnomaliesByTime(mockAnomalies, 1, 1)).toEqual([]);
        expect(filterAnomaliesByTime(mockAnomalies, 10, 10)).toEqual([]);
    });

    test("getAnomaliesAtTime should return empty array for invalid anomalies", () => {
        expect(getAnomaliesAtTime((null as unknown) as Anomaly[], 1)).toEqual(
            []
        );
    });

    test("getAnomaliesAtTime should return empty array for empty anomalies", () => {
        expect(getAnomaliesAtTime([], 1)).toEqual([]);
    });

    test("getAnomaliesAtTime should return appropriate anomaly array for anomalies and invalid time", () => {
        expect(
            getAnomaliesAtTime(mockAnomalies, (null as unknown) as number)
        ).toEqual(mockAnomalies);
    });

    test("getAnomaliesAtTime should return appropriate anomaly array for anomalies and time", () => {
        expect(getAnomaliesAtTime(mockAnomalies, 2)).toEqual([mockAnomaly1]);
        expect(getAnomaliesAtTime(mockAnomalies, 2.5)).toEqual([mockAnomaly1]);
        expect(getAnomaliesAtTime(mockAnomalies, 3)).toEqual([
            mockAnomaly1,
            mockAnomaly3,
        ]);
        expect(getAnomaliesAtTime(mockAnomalies, 8)).toEqual([
            mockAnomaly3,
            mockAnomaly2,
        ]);
        expect(getAnomaliesAtTime(mockAnomalies, 8.5)).toEqual([mockAnomaly2]);
        expect(getAnomaliesAtTime(mockAnomalies, 9)).toEqual([mockAnomaly2]);
        expect(getAnomaliesAtTime(mockAnomalies, 1)).toEqual([]);
        expect(getAnomaliesAtTime(mockAnomalies, 10)).toEqual([]);
    });
});

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
    id: 1,
    startTime: 2,
    endTime: 3,
    avgCurrentVal: 4,
    avgBaselineVal: 5,
    alert: {
        id: 6,
        name: "testNameAlert6",
    },
} as Anomaly;

const mockAnomaly2 = {
    id: 7,
    startTime: 8,
    endTime: 9,
    alert: {
        id: 10,
    },
} as Anomaly;

const mockAnomaly3 = {
    id: 11,
    startTime: 3,
    endTime: 8,
} as Anomaly;

const mockAnomaly4 = {
    id: 12,
} as Anomaly;

const mockAnomalies = [mockAnomaly1, mockAnomaly2, mockAnomaly3, mockAnomaly4];

const mockAnomalyCardData1 = {
    id: 1,
    name: "label.anomaly label.entity-id",
    alertId: 6,
    alertName: "testNameAlert6",
    current: "4",
    predicted: "5",
    deviation: "-0.2",
    negativeDeviation: true,
    duration: "2 3",
    startTime: "2",
    endTime: "3",
};

const mockAnomalyCardData2 = {
    id: 7,
    name: "label.anomaly label.entity-id",
    alertId: 10,
    alertName: "label.no-data-marker",
    current: "label.no-data-marker",
    predicted: "label.no-data-marker",
    deviation: "label.no-data-marker",
    negativeDeviation: false,
    duration: "8 9",
    startTime: "8",
    endTime: "9",
};

const mockAnomalyCardData3 = {
    id: 11,
    name: "label.anomaly label.entity-id",
    alertId: -1,
    alertName: "label.no-data-marker",
    current: "label.no-data-marker",
    predicted: "label.no-data-marker",
    deviation: "label.no-data-marker",
    negativeDeviation: false,
    duration: "3 8",
    startTime: "3",
    endTime: "8",
};

const mockAnomalyCardData4 = {
    id: 12,
    name: "label.anomaly label.entity-id",
    alertId: -1,
    alertName: "label.no-data-marker",
    current: "label.no-data-marker",
    predicted: "label.no-data-marker",
    deviation: "label.no-data-marker",
    negativeDeviation: false,
    duration: "label.no-data-marker",
    startTime: "label.no-data-marker",
    endTime: "label.no-data-marker",
};

const mockAnomalyCardDatas = [
    mockAnomalyCardData1,
    mockAnomalyCardData2,
    mockAnomalyCardData3,
    mockAnomalyCardData4,
];

const mockSearchWords = ["testNameAlert6", "8 9"];
