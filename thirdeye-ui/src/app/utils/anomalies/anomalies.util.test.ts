/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import i18n from "i18next";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";
import {
    createAlertEvaluation,
    createEmptyUiAnomaly,
    filterAnomalies,
    filterAnomaliesByTime,
    getAnomaliesAtTime,
    getAnomalyName,
    getUiAnomalies,
    getUiAnomaly,
} from "./anomalies.util";

jest.mock("i18next", () => ({
    t: jest.fn().mockImplementation((key) => key),
}));

jest.mock("@startree-ui/platform-ui", () => ({
    formatLargeNumberV1: jest.fn().mockImplementation((num) => num.toString()),
    formatPercentageV1: jest.fn().mockImplementation((num) => num.toString()),
    formatDateAndTimeV1: jest
        .fn()
        .mockImplementation((date) => date.toString()),
    formatDurationV1: jest
        .fn()
        .mockImplementation(
            (startTime, endTime) =>
                `${startTime.toString()} ${endTime.toString()}`
        ),
}));

describe("Anomalies Util", () => {
    it("getAnomalyName should return appropriate name for invalid anomaly", () => {
        expect(getAnomalyName(null as unknown as Anomaly)).toEqual(
            "label.anomaly"
        );
    });

    it("getAnomalyName should return appropriate name for anomaly with invalid id", () => {
        expect(getAnomalyName({} as Anomaly)).toEqual("label.anomaly");
    });

    it("getAnomalyName should return appropriate name for anomaly", () => {
        expect(getAnomalyName(mockAnomaly1)).toEqual(
            "label.anomaly label.entity-id"
        );
        expect(i18n.t).toHaveBeenCalledWith("label.entity-id", { id: 1 });
    });

    it("createEmptyUiAnomaly should create appropriate UI anomaly", () => {
        expect(createEmptyUiAnomaly()).toEqual(mockEmptyUiAnomaly);
    });

    it("createAlertEvaluation should create appropriate alert evaluation", () => {
        expect(createAlertEvaluation(1, 2, 3)).toEqual({
            alert: {
                id: 1,
            },
            start: 2,
            end: 3,
        });
    });

    it("getUiAnomaly should return empty UI anomaly for invalid anomaly", () => {
        expect(getUiAnomaly(null as unknown as Anomaly)).toEqual(
            mockEmptyUiAnomaly
        );
    });

    it("getUiAnomaly should return appropriate UI anomaly for anomaly", () => {
        expect(getUiAnomaly(mockAnomaly1)).toEqual(mockUiAnomaly1);
    });

    it("getUiAnomalies should return empty array for invalid anomalies", () => {
        expect(getUiAnomalies(null as unknown as Anomaly[])).toEqual([]);
    });

    it("getUiAnomalies should return empty array for empty anomalies", () => {
        expect(getUiAnomalies([])).toEqual([]);
    });

    it("getUiAnomalies should return appropriate UI anomalies for anomalies", () => {
        expect(getUiAnomalies(mockAnomalies)).toEqual(mockUiAnomalies);
    });

    it("filterAnomalies should return empty array for invalid UI anomalies", () => {
        expect(
            filterAnomalies(null as unknown as UiAnomaly[], mockSearchWords)
        ).toEqual([]);
    });

    it("filterAnomalies should return empty array for empty UI anomalies", () => {
        expect(filterAnomalies([], mockSearchWords)).toEqual([]);
    });

    it("filterAnomalies should return appropriate UI anomalies for UI anomalies and invalid search words", () => {
        expect(
            filterAnomalies(mockUiAnomalies, null as unknown as string[])
        ).toEqual(mockUiAnomalies);
    });

    it("filterAnomalies should return appropriate UI anomalies for UI anomalies and empty search words", () => {
        expect(filterAnomalies(mockUiAnomalies, [])).toEqual(mockUiAnomalies);
    });

    it("filterAnomalies should return appropriate UI anomalies for UI anomalies and search words", () => {
        expect(filterAnomalies(mockUiAnomalies, mockSearchWords)).toEqual([
            mockUiAnomaly1,
            mockUiAnomaly2,
        ]);
    });

    it("filterAnomaliesByTime should return empty array for invalid anomalies", () => {
        expect(
            filterAnomaliesByTime(null as unknown as Anomaly[], 1, 2)
        ).toEqual([]);
    });

    it("filterAnomaliesByTime should return empty array for empty anomalies", () => {
        expect(filterAnomaliesByTime([], 1, 2)).toEqual([]);
    });

    it("filterAnomaliesByTime should return appropriate anomalies for anomalies and invalid start and end time", () => {
        expect(
            filterAnomaliesByTime(mockAnomalies, null as unknown as number, 1)
        ).toEqual(mockAnomalies);
        expect(
            filterAnomaliesByTime(mockAnomalies, 1, null as unknown as number)
        ).toEqual(mockAnomalies);
        expect(
            filterAnomaliesByTime(
                mockAnomalies,
                null as unknown as number,
                null as unknown as number
            )
        ).toEqual(mockAnomalies);
    });

    it("filterAnomaliesByTime should return appropriate anomalies for anomalies and start and end time", () => {
        expect(filterAnomaliesByTime(mockAnomalies, 2, 2)).toEqual([
            mockAnomaly1,
        ]);
        expect(filterAnomaliesByTime(mockAnomalies, 2, 3)).toEqual([
            mockAnomaly1,
            mockAnomaly3,
        ]);
        expect(filterAnomaliesByTime(mockAnomalies, 8, 9)).toEqual([
            mockAnomaly3,
            mockAnomaly2,
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
        expect(filterAnomaliesByTime(mockAnomalies, 0, 1)).toEqual([]);
        expect(filterAnomaliesByTime(mockAnomalies, 10, 11)).toEqual([]);
    });

    it("getAnomaliesAtTime should return empty array for invalid anomalies", () => {
        expect(getAnomaliesAtTime(null as unknown as Anomaly[], 1)).toEqual([]);
    });

    it("getAnomaliesAtTime should return empty array for empty anomalies", () => {
        expect(getAnomaliesAtTime([], 1)).toEqual([]);
    });

    it("getAnomaliesAtTime should return appropriate anomalies for anomalies and invalid time", () => {
        expect(
            getAnomaliesAtTime(mockAnomalies, null as unknown as number)
        ).toEqual(mockAnomalies);
    });

    it("getAnomaliesAtTime should return appropriate anomalies for anomalies and time", () => {
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

const mockEmptyUiAnomaly = {
    id: -1,
    name: "label.no-data-marker",
    alertName: "label.no-data-marker",
    alertId: -1,
    metricId: -1,
    metricName: "label.no-data-marker",
    current: "label.no-data-marker",
    currentVal: -1,
    predicted: "label.no-data-marker",
    predictedVal: -1,
    deviation: "label.no-data-marker",
    deviationVal: -1,
    negativeDeviation: false,
    duration: "label.no-data-marker",
    durationVal: 0,
    startTime: "label.no-data-marker",
    endTime: "label.no-data-marker",
    startTimeVal: -1,
    endTimeVal: -1,
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

const mockAnomaly5 = {
    id: 1,
    startTime: 100,
    endTime: 110,
    avgCurrentVal: 0,
    avgBaselineVal: 5,
} as Anomaly;

const mockAnomalies = [
    mockAnomaly1,
    mockAnomaly2,
    mockAnomaly3,
    mockAnomaly4,
    mockAnomaly5,
];

const mockUiAnomaly1 = {
    id: 1,
    name: "label.anomaly label.entity-id",
    alertId: 6,
    alertName: "testNameAlert6",
    metricId: -1,
    metricName: "label.no-data-marker",
    current: "4",
    predicted: "5",
    deviation: "-0.2",
    currentVal: 4,
    predictedVal: 5,
    deviationVal: -0.2,
    negativeDeviation: true,
    duration: "2 3",
    durationVal: 1,
    startTime: "2",
    endTime: "3",
    startTimeVal: 2,
    endTimeVal: 3,
};

const mockUiAnomaly2 = {
    id: 7,
    name: "label.anomaly label.entity-id",
    alertId: 10,
    alertName: "label.no-data-marker",
    metricId: -1,
    metricName: "label.no-data-marker",
    current: "label.no-data-marker",
    predicted: "label.no-data-marker",
    deviation: "label.no-data-marker",
    currentVal: -1,
    predictedVal: -1,
    deviationVal: -1,
    negativeDeviation: false,
    duration: "8 9",
    durationVal: 1,
    startTime: "8",
    endTime: "9",
    startTimeVal: 8,
    endTimeVal: 9,
};

const mockUiAnomaly3 = {
    id: 11,
    name: "label.anomaly label.entity-id",
    alertId: -1,
    alertName: "label.no-data-marker",
    metricId: -1,
    metricName: "label.no-data-marker",
    current: "label.no-data-marker",
    predicted: "label.no-data-marker",
    deviation: "label.no-data-marker",
    currentVal: -1,
    predictedVal: -1,
    deviationVal: -1,
    negativeDeviation: false,
    duration: "3 8",
    durationVal: 5,
    startTime: "3",
    endTime: "8",
    startTimeVal: 3,
    endTimeVal: 8,
};

const mockUiAnomaly4 = {
    id: 12,
    name: "label.anomaly label.entity-id",
    alertId: -1,
    alertName: "label.no-data-marker",
    metricId: -1,
    metricName: "label.no-data-marker",
    current: "label.no-data-marker",
    predicted: "label.no-data-marker",
    deviation: "label.no-data-marker",
    currentVal: -1,
    predictedVal: -1,
    deviationVal: -1,
    negativeDeviation: false,
    duration: "label.no-data-marker",
    durationVal: 0,
    startTime: "label.no-data-marker",
    endTime: "label.no-data-marker",
    startTimeVal: -1,
    endTimeVal: -1,
};

const mockUiAnomaly5 = {
    id: 1,
    name: "label.anomaly label.entity-id",
    alertId: -1,
    alertName: "label.no-data-marker",
    current: "0",
    metricId: -1,
    metricName: "label.no-data-marker",
    predicted: "5",
    deviation: "-1",
    currentVal: 0,
    predictedVal: 5,
    deviationVal: -1,
    negativeDeviation: true,
    duration: "100 110",
    durationVal: 10,
    startTime: "100",
    endTime: "110",
    startTimeVal: 100,
    endTimeVal: 110,
};

const mockUiAnomalies = [
    mockUiAnomaly1,
    mockUiAnomaly2,
    mockUiAnomaly3,
    mockUiAnomaly4,
    mockUiAnomaly5,
];

const mockSearchWords = ["testNameAlert6", "8 9"];
