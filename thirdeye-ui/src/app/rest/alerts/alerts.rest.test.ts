import axios from "axios";
import { Alert, AlertEvaluation } from "../dto/alert.interfaces";
import {
    createAlert,
    createAlerts,
    deleteAlert,
    getAlert,
    getAlertEvaluation,
    getAllAlerts,
    updateAlert,
    updateAlerts,
} from "./alerts.rest";

jest.mock("axios");

describe("Alerts REST", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });

    test("getAlert should invoke axios.get with appropriate input and return appropriate alert", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: mockAlertResponse,
        });

        await expect(getAlert(1)).resolves.toEqual(mockAlertResponse);

        expect(axios.get).toHaveBeenCalledWith("/api/alerts/1");
    });

    test("getAlert should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAlert(1)).rejects.toThrow("testError");
    });

    test("getAllAlerts should invoke axios.get with appropriate input and return appropriate alerts", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockAlertResponse],
        });

        await expect(getAllAlerts()).resolves.toEqual([mockAlertResponse]);

        expect(axios.get).toHaveBeenCalledWith("/api/alerts");
    });

    test("getAllAlerts should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAllAlerts()).rejects.toThrow("testError");
    });

    test("createAlert should invoke axios.post with appropriate input and return appropriate alert", async () => {
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

    test("createAlert should throw encountered error", async () => {
        jest.spyOn(axios, "post").mockRejectedValue(mockError);

        await expect(createAlert(mockAlertRequest)).rejects.toThrow(
            "testError"
        );
    });

    test("createAlerts should invoke axios.post with appropriate input and return appropriate alerts", async () => {
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

    test("createAlerts should throw encountered error", async () => {
        jest.spyOn(axios, "post").mockRejectedValue(mockError);

        await expect(createAlerts([mockAlertRequest])).rejects.toThrow(
            "testError"
        );
    });

    test("updateAlert should invoke axios.put with appropriate input and return appropriate alert", async () => {
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

    test("updateAlert should throw encountered error", async () => {
        jest.spyOn(axios, "put").mockRejectedValue(mockError);

        await expect(updateAlert(mockAlertRequest)).rejects.toThrow(
            "testError"
        );
    });

    test("updateAlerts should invoke axios.put with appropriate input and return appropriate alerts", async () => {
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

    test("updateAlerts should throw encountered error", async () => {
        jest.spyOn(axios, "put").mockRejectedValue(mockError);

        await expect(updateAlerts([mockAlertRequest])).rejects.toThrow(
            "testError"
        );
    });

    test("deleteAlert should invoke axios.delete with appropriate input and return appropriate alert", async () => {
        jest.spyOn(axios, "delete").mockResolvedValue({
            data: mockAlertResponse,
        });

        await expect(deleteAlert(1)).resolves.toEqual(mockAlertResponse);

        expect(axios.delete).toHaveBeenCalledWith("/api/alerts/1");
    });

    test("deleteAlert should throw encountered error", async () => {
        jest.spyOn(axios, "delete").mockRejectedValue(mockError);

        await expect(deleteAlert(1)).rejects.toThrow("testError");
    });

    test("getAlertEvaluation should invoke axios.post with appropriate input and return appropriate alert evaluation", async () => {
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

    test("getAlertEvaluation should throw encountered error", async () => {
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
