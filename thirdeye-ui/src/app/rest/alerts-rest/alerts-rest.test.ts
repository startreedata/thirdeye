import axios from "axios";
import { Alert, AlertEvaluation } from "../dto/alert.interfaces";
import {
    createAlert,
    deleteAlert,
    getAlert,
    getAlertEvaluation,
    getAllAlerts,
    updateAlert,
} from "./alerts-rest";

jest.mock("axios");

describe("Alerts REST", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });

    test("getAlert should invoke axios.get with appropriate input and return appropriate alert", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: mockAlertResponse,
        });

        expect(await getAlert(1)).toEqual(mockAlertResponse);
        expect(axios.get).toHaveBeenCalledWith("/api/alerts/1");
    });

    test("getAlert should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAlert(1)).rejects.toThrow("testErrorMessage");
    });

    test("getAllAlerts should invoke axios.get with appropriate input and return appropriate alert array", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockAlertResponse],
        });

        expect(await getAllAlerts()).toEqual([mockAlertResponse]);
        expect(axios.get).toHaveBeenCalledWith("/api/alerts");
    });

    test("getAllAlerts should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAllAlerts()).rejects.toThrow("testErrorMessage");
    });

    test("createAlert should invoke axios.post with appropriate input and return appropriate alert", async () => {
        jest.spyOn(axios, "post").mockResolvedValue({
            data: [mockAlertResponse],
        });

        expect(await createAlert(mockAlertRequest)).toEqual(mockAlertResponse);
        expect(axios.post).toHaveBeenCalledWith("/api/alerts", [
            mockAlertRequest,
        ]);
    });

    test("createAlert should throw encountered error", async () => {
        jest.spyOn(axios, "post").mockRejectedValue(mockError);

        await expect(createAlert(mockAlertRequest)).rejects.toThrow(
            "testErrorMessage"
        );
    });

    test("updateAlert should invoke axios.put with appropriate input and return appropriate alert", async () => {
        jest.spyOn(axios, "put").mockResolvedValue({
            data: [mockAlertResponse],
        });

        expect(await updateAlert(mockAlertRequest)).toEqual(mockAlertResponse);
        expect(axios.put).toHaveBeenCalledWith("/api/alerts", [
            mockAlertRequest,
        ]);
    });

    test("updateAlert should throw encountered error", async () => {
        jest.spyOn(axios, "put").mockRejectedValue(mockError);

        await expect(updateAlert(mockAlertRequest)).rejects.toThrow(
            "testErrorMessage"
        );
    });

    test("deleteAlert should invoke axios.delete with appropriate input and return appropriate alert", async () => {
        jest.spyOn(axios, "delete").mockResolvedValue({
            data: mockAlertResponse,
        });

        expect(await deleteAlert(1)).toEqual(mockAlertResponse);
        expect(axios.delete).toHaveBeenCalledWith("/api/alerts/1");
    });

    test("deleteAlert should throw encountered error", async () => {
        jest.spyOn(axios, "delete").mockRejectedValue(mockError);

        await expect(deleteAlert(1)).rejects.toThrow("testErrorMessage");
    });

    test("getAlertEvaluation should invoke axios.post with appropriate input and return appropriate alert evaluation", async () => {
        jest.spyOn(axios, "post").mockResolvedValue({
            data: mockAlertEvaluationResponse,
        });

        expect(await getAlertEvaluation(mockAlertEvaluationRequest)).toEqual(
            mockAlertEvaluationResponse
        );
        expect(axios.post).toHaveBeenCalledWith(
            "/api/alerts/evaluate",
            mockAlertEvaluationRequest
        );
    });

    test("getAlertEvaluation should throw encountered error", async () => {
        jest.spyOn(axios, "post").mockRejectedValue(mockError);

        await expect(
            getAlertEvaluation(mockAlertEvaluationRequest)
        ).rejects.toThrow("testErrorMessage");
    });
});

const mockAlertRequest: Alert = {
    name: "testAlertNameRequest",
} as Alert;

const mockAlertResponse: Alert = {
    name: "testAlertNameResponse",
} as Alert;

const mockAlertEvaluationRequest: AlertEvaluation = {
    alert: mockAlertRequest,
} as AlertEvaluation;

const mockAlertEvaluationResponse: AlertEvaluation = {
    alert: mockAlertResponse,
} as AlertEvaluation;

const mockError = new Error("testErrorMessage");
