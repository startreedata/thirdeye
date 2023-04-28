/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import axios from "axios";
import {
    AnomalyFeedback,
    AnomalyFeedbackType,
    AnomalyStats,
} from "../dto/anomaly.interfaces";
import {
    deleteAnomaly,
    getAnomalies,
    getAnomaly,
    getAnomalyStats,
    updateAnomalyFeedback,
} from "./anomalies.rest";

jest.mock("axios");

describe("Anomalies REST", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });

    it("getAnomaly should invoke axios.get with appropriate input and return appropriate anomaly", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: mockAnomaly,
        });

        await expect(getAnomaly(1)).resolves.toEqual(mockAnomaly);

        expect(axios.get).toHaveBeenCalledWith("/api/anomalies/1");
    });

    it("getAnomaly should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAnomaly(1)).rejects.toThrow("testError");
    });

    it("getAnomalyStats should invoke axios.get with appropriate input and return appropriate anomaly", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: mockAnomalyStats,
        });

        await expect(
            getAnomalyStats({ startTime: 1, endTime: 2 })
        ).resolves.toEqual(mockAnomalyStats);

        expect(axios.get).toHaveBeenCalledWith(
            "/api/anomalies/stats?startTime=1&endTime=2"
        );
    });

    it("getAnomalyStats should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(
            getAnomalyStats({ startTime: 1, endTime: 2 })
        ).rejects.toThrow("testError");
    });

    it("getAnomalies should invoke axios.get with appropriate input and return appropriate anomalies", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockAnomaly],
        });

        await expect(getAnomalies()).resolves.toEqual([mockAnomaly]);

        expect(axios.get).toHaveBeenCalledWith("/api/anomalies?isChild=false");
    });

    it("getAnomalies should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAnomalies()).rejects.toThrow("testError");
    });

    it("getAnomalies with alertId should invoke axios.get with appropriate input and return appropriate anomalies", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockAnomaly],
        });

        await expect(getAnomalies({ alertId: 1 })).resolves.toEqual([
            mockAnomaly,
        ]);

        expect(axios.get).toHaveBeenCalledWith(
            "/api/anomalies?isChild=false&alert.id=1"
        );
    });

    it("getAnomalies with alertId should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAnomalies({ alertId: 1 })).rejects.toThrow("testError");
    });

    it("getAnomalies with start and end should invoke axios.get with appropriate input and return appropriate anomalies", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockAnomaly],
        });

        await expect(
            getAnomalies({ startTime: 1, endTime: 2 })
        ).resolves.toEqual([mockAnomaly]);

        // /api/anomalies?isChild=false&startTime=[gte]1&endTime=[lte]2
        expect(axios.get).toHaveBeenCalledWith(
            "/api/anomalies?isChild=false&startTime=%5Bgte%5D1&endTime=%5Blte%5D2"
        );
    });

    it("getAnomalies with start and end should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(
            getAnomalies({ startTime: 1, endTime: 2 })
        ).rejects.toThrow("testError");
    });

    it("getAnomalies with start, end, and alert id should invoke axios.get with appropriate input and return appropriate anomalies", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockAnomaly],
        });

        await expect(
            getAnomalies({ alertId: 1, startTime: 2, endTime: 3 })
        ).resolves.toEqual([mockAnomaly]);

        // /api/anomalies?isChild=false&alert.id=1&startTime=[gte]2&endTime=[lte]3
        expect(axios.get).toHaveBeenCalledWith(
            "/api/anomalies?isChild=false&alert.id=1&startTime=%5Bgte%5D2&endTime=%5Blte%5D3"
        );
    });

    it("getAnomalies with alert if, start, and end should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(
            getAnomalies({ alertId: 1, startTime: 2, endTime: 3 })
        ).rejects.toThrow("testError");
    });

    it("deleteAnomaly should invoke axios.delete with appropriate input and return appropriate anomaly", async () => {
        jest.spyOn(axios, "delete").mockResolvedValue({
            data: mockAnomaly,
        });

        await expect(deleteAnomaly(1)).resolves.toEqual(mockAnomaly);

        expect(axios.delete).toHaveBeenCalledWith("/api/anomalies/1");
    });

    it("deleteAnomaly should throw encountered error", async () => {
        jest.spyOn(axios, "delete").mockRejectedValue(mockError);

        await expect(deleteAnomaly(1)).rejects.toThrow("testError");
    });

    it("updateAnomalyFeedback should invoke axios.post with appropriate input and return appropriate AnomalyFeedback", async () => {
        jest.spyOn(axios, "post").mockResolvedValue({
            data: mockAnomalyFeedback,
        });

        await expect(
            updateAnomalyFeedback(1, mockAnomalyFeedback)
        ).resolves.toEqual(mockAnomalyFeedback);

        expect(axios.post).toHaveBeenCalledWith(
            "/api/anomalies/1/feedback",
            mockAnomalyFeedback
        );
    });

    it("updateAnomalyFeedback should throw encountered error", async () => {
        jest.spyOn(axios, "post").mockRejectedValue(mockError);

        await expect(
            updateAnomalyFeedback(1, mockAnomalyFeedback)
        ).rejects.toThrow("testError");
    });
});

const mockAnomaly = {
    id: 1,
};

const mockAnomalyStats = {
    totalCount: 12,
    countWithFeedback: 4,
    feedbackStats: {
        ANOMALY: 3,
        ANOMALY_EXPECTED: 0,
        NOT_ANOMALY: 1,
        ANOMALY_NEW_TREND: 0,
        NO_FEEDBACK: 0,
    },
} as AnomalyStats;

const mockError = new Error("testError");

const mockAnomalyFeedback = {
    type: AnomalyFeedbackType.ANOMALY,
    comment: "hello world",
} as AnomalyFeedback;
