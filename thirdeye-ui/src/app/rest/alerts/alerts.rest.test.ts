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
import axios from "axios";
import { Alert, AlertEvaluation } from "../dto/alert.interfaces";
import {
    createAlert,
    createAlerts,
    deleteAlert,
    getAlert,
    getAlertEvaluation,
    getAlertInsight,
    getAllAlerts,
    resetAlert,
    updateAlert,
    updateAlerts,
} from "./alerts.rest";

jest.mock("axios");

describe("Alerts REST", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });

    it("getAlert should invoke axios.get with appropriate input and return appropriate alert", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: mockAlertResponse,
        });

        await expect(getAlert(1)).resolves.toEqual(mockAlertResponse);

        expect(axios.get).toHaveBeenCalledWith("/api/alerts/1");
    });

    it("getAlert should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAlert(1)).rejects.toThrow("testError");
    });

    it("getAlertInsight should invoke axios.get with appropriate input and return appropriate alert insight", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: mockAlertResponse,
        });

        await expect(getAlertInsight(1)).resolves.toEqual(mockAlertResponse);

        expect(axios.get).toHaveBeenCalledWith("/api/alerts/1/insights");
    });

    it("getAlertInsight should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAlertInsight(1)).rejects.toThrow("testError");
    });

    it("getAllAlerts should invoke axios.get with appropriate input and return appropriate alerts", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockAlertResponse],
        });

        await expect(getAllAlerts()).resolves.toEqual([mockAlertResponse]);

        expect(axios.get).toHaveBeenCalledWith("/api/alerts");
    });

    it("getAllAlerts should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAllAlerts()).rejects.toThrow("testError");
    });

    it("createAlert should invoke axios.post with appropriate input and return appropriate alert", async () => {
        jest.spyOn(axios, "post").mockResolvedValue({
            data: [mockAlertResponse],
        });

        await expect(createAlert(mockAlertRequest)).resolves.toEqual(
            mockAlertResponse
        );

        expect(axios.post).toHaveBeenCalledWith("/api/alerts", [
            mockAlertRequest,
        ]);
    });

    it("createAlert should throw encountered error", async () => {
        jest.spyOn(axios, "post").mockRejectedValue(mockError);

        await expect(createAlert(mockAlertRequest)).rejects.toThrow(
            "testError"
        );
    });

    it("createAlerts should invoke axios.post with appropriate input and return appropriate alerts", async () => {
        jest.spyOn(axios, "post").mockResolvedValue({
            data: [mockAlertResponse],
        });

        await expect(createAlerts([mockAlertRequest])).resolves.toEqual([
            mockAlertResponse,
        ]);

        expect(axios.post).toHaveBeenCalledWith("/api/alerts", [
            mockAlertRequest,
        ]);
    });

    it("createAlerts should throw encountered error", async () => {
        jest.spyOn(axios, "post").mockRejectedValue(mockError);

        await expect(createAlerts([mockAlertRequest])).rejects.toThrow(
            "testError"
        );
    });

    it("updateAlert should invoke axios.put with appropriate input and return appropriate alert", async () => {
        jest.spyOn(axios, "put").mockResolvedValue({
            data: [mockAlertResponse],
        });

        await expect(updateAlert(mockAlertRequest)).resolves.toEqual(
            mockAlertResponse
        );

        expect(axios.put).toHaveBeenCalledWith("/api/alerts", [
            mockAlertRequest,
        ]);
    });

    it("updateAlert should throw encountered error", async () => {
        jest.spyOn(axios, "put").mockRejectedValue(mockError);

        await expect(updateAlert(mockAlertRequest)).rejects.toThrow(
            "testError"
        );
    });

    it("updateAlerts should invoke axios.put with appropriate input and return appropriate alerts", async () => {
        jest.spyOn(axios, "put").mockResolvedValue({
            data: [mockAlertResponse],
        });

        await expect(updateAlerts([mockAlertRequest])).resolves.toEqual([
            mockAlertResponse,
        ]);

        expect(axios.put).toHaveBeenCalledWith("/api/alerts", [
            mockAlertRequest,
        ]);
    });

    it("updateAlerts should throw encountered error", async () => {
        jest.spyOn(axios, "put").mockRejectedValue(mockError);

        await expect(updateAlerts([mockAlertRequest])).rejects.toThrow(
            "testError"
        );
    });

    it("deleteAlert should invoke axios.delete with appropriate input and return appropriate alert", async () => {
        jest.spyOn(axios, "delete").mockResolvedValue({
            data: mockAlertResponse,
        });

        await expect(deleteAlert(1)).resolves.toEqual(mockAlertResponse);

        expect(axios.delete).toHaveBeenCalledWith("/api/alerts/1");
    });

    it("deleteAlert should throw encountered error", async () => {
        jest.spyOn(axios, "delete").mockRejectedValue(mockError);

        await expect(deleteAlert(1)).rejects.toThrow("testError");
    });

    it("resetAlert should invoke axios.delete with appropriate input and return appropriate alert", async () => {
        jest.spyOn(axios, "post").mockResolvedValue({
            data: mockAlertResponse,
        });

        await expect(resetAlert(1)).resolves.toEqual(mockAlertResponse);

        expect(axios.post).toHaveBeenCalledWith("/api/alerts/1/reset");
    });

    it("resetAlert should throw encountered error", async () => {
        jest.spyOn(axios, "post").mockRejectedValue(mockError);

        await expect(resetAlert(1)).rejects.toThrow("testError");
    });

    it("getAlertEvaluation should invoke axios.post with appropriate input and return appropriate alert evaluation", async () => {
        jest.spyOn(axios, "post").mockResolvedValue({
            data: mockAlertEvaluationResponse,
        });

        await expect(
            getAlertEvaluation(mockAlertEvaluationRequest)
        ).resolves.toEqual(mockAlertEvaluationResponse);

        expect(axios.post).toHaveBeenCalledWith(
            "/api/alerts/evaluate",
            mockAlertEvaluationRequest
        );
    });

    it("getAlertEvaluation should invoke axios.post with appropriate input and return appropriate alert evaluation and filter properties", async () => {
        jest.spyOn(axios, "post").mockResolvedValue({
            data: mockAlertEvaluationResponse,
        });

        await expect(
            getAlertEvaluation(mockAlertEvaluationRequest, [
                "hello=world",
                "foo=bar",
            ])
        ).resolves.toEqual(mockAlertEvaluationResponse);

        expect(axios.post).toHaveBeenCalledWith("/api/alerts/evaluate", {
            ...mockAlertEvaluationRequest,
            evaluationContext: { filters: ["hello=world", "foo=bar"] },
        });
    });

    it("getAlertEvaluation should throw encountered error", async () => {
        jest.spyOn(axios, "post").mockRejectedValue(mockError);

        await expect(
            getAlertEvaluation(mockAlertEvaluationRequest)
        ).rejects.toThrow("testError");
    });
});

const mockAlertRequest = {
    name: "testNameAlertRequest",
} as Alert;

const mockAlertResponse = {
    name: "testNameAlertResponse",
};

const mockAlertEvaluationRequest = {
    alert: mockAlertRequest,
} as AlertEvaluation;

const mockAlertEvaluationResponse = {
    alert: mockAlertResponse,
};

const mockError = new Error("testError");
