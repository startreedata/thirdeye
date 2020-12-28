import axios from "axios";
import { Alert, AlertEvaluation } from "../dto/alert.interfaces";
import {
    createAlert,
    deleteAlert,
    getAlert,
    getAlertEvaluation,
    getAllAlerts,
    updateAlert,
} from "./alert-rest";

jest.mock("axios");

describe("Alert REST", () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    afterAll(() => {
        jest.restoreAllMocks();
    });

    test("getAlert shall invoke axios.get with appropriate input and return result", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({ data: mockAlertResponse });

        const response = await getAlert(1);

        expect(axios.get).toHaveBeenCalledWith("/api/alerts/1");
        expect(response).toEqual(mockAlertResponse);
    });

    test("getAlert shall throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAlert(1)).rejects.toThrow("testErrorMessage");
    });

    test("getAllAlerts shall invoke axios.get with appropriate input and return result", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockAlertResponse],
        });

        const response = await getAllAlerts();

        expect(axios.get).toHaveBeenCalledWith("/api/alerts");
        expect(response).toEqual([mockAlertResponse]);
    });

    test("getAllAlerts shall throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAllAlerts()).rejects.toThrow("testErrorMessage");
    });

    test("createAlert shall invoke axios.post with appropriate input and return result", async () => {
        jest.spyOn(axios, "post").mockResolvedValue({
            data: [mockAlertResponse],
        });

        const response = await createAlert(mockAlertRequest);

        expect(axios.post).toHaveBeenCalledWith("/api/alerts", [
            mockAlertRequest,
        ]);
        expect(response).toEqual(mockAlertResponse);
    });

    test("createAlert shall throw encountered error", async () => {
        jest.spyOn(axios, "post").mockRejectedValue(mockError);

        await expect(createAlert(mockAlertRequest)).rejects.toThrow(
            "testErrorMessage"
        );
    });

    test("updateAlert shall invoke axios.put with appropriate input and return result", async () => {
        jest.spyOn(axios, "put").mockResolvedValue({
            data: [mockAlertResponse],
        });

        const response = await updateAlert(mockAlertRequest);

        expect(axios.put).toHaveBeenCalledWith("/api/alerts", [
            mockAlertRequest,
        ]);
        expect(response).toEqual(mockAlertResponse);
    });

    test("updateAlert shall throw encountered error", async () => {
        jest.spyOn(axios, "put").mockRejectedValue(mockError);

        await expect(updateAlert(mockAlertRequest)).rejects.toThrow(
            "testErrorMessage"
        );
    });

    test("deleteAlert shall invoke axios.delete with appropriate input and return result", async () => {
        jest.spyOn(axios, "delete").mockResolvedValue({
            data: mockAlertResponse,
        });

        const response = await deleteAlert(1);

        expect(axios.delete).toHaveBeenCalledWith("/api/alerts/1");
        expect(response).toEqual(mockAlertResponse);
    });

    test("deleteAlert shall throw encountered error", async () => {
        jest.spyOn(axios, "delete").mockRejectedValue(mockError);

        await expect(deleteAlert(1)).rejects.toThrow("testErrorMessage");
    });

    test("getAlertEvaluation shall invoke axios.post with appropriate input and return result", async () => {
        jest.spyOn(axios, "post").mockResolvedValue({
            data: mockAlertEvaluationResponse,
        });

        const response = await getAlertEvaluation(mockAlertEvaluationRequest);

        expect(axios.post).toHaveBeenCalledWith(
            "/api/alerts/evaluate",
            mockAlertEvaluationRequest
        );
        expect(response).toEqual(mockAlertEvaluationResponse);
    });

    test("getAlertEvaluation shall throw encountered error", async () => {
        jest.spyOn(axios, "post").mockRejectedValue(mockError);

        await expect(
            getAlertEvaluation(mockAlertEvaluationRequest)
        ).rejects.toThrow("testErrorMessage");
    });
});

const mockAlertRequest = {
    name: "testAlertRequest",
} as Alert;
const mockAlertResponse = {
    name: "testAlertResponse",
} as Alert;
const mockAlertEvaluationRequest = {
    alert: mockAlertRequest,
} as AlertEvaluation;
const mockAlertEvaluationResponse = {
    alert: mockAlertResponse,
} as AlertEvaluation;
const mockError = new Error("testErrorMessage");
