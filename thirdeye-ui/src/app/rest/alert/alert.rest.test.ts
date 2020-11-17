import axios from "axios";
import { Alert, AlertEvaluation } from "../dto/alert.interfaces";
import {
    createAlert,
    deleteAlert,
    getAlert,
    getAlertPreview,
    getAllAlerts,
    updateAlert,
} from "./alert.rest";

jest.mock("axios");

const mockAlertRequest: Alert = {
    name: "testAlertRequest",
} as Alert;

const mockAlertResponse: Alert = {
    name: "testAlertResponse",
} as Alert;

const mockAlertEvaluationRequest: AlertEvaluation = {
    alert: mockAlertRequest,
} as AlertEvaluation;

const mockAlertEvaluationResponse: AlertEvaluation = {
    alert: mockAlertResponse,
} as AlertEvaluation;

const mockError = {
    message: "testError",
};

describe("Alert REST", () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    afterAll(() => {
        jest.restoreAllMocks();
    });

    test("getAlert shall invoke axios.get with appropriate input and return result", async () => {
        (axios.get as jest.Mock).mockResolvedValue({ data: mockAlertResponse });

        const response = await getAlert(1);

        expect(axios.get).toHaveBeenCalledWith("/api/alerts/1");
        expect(response).toEqual(mockAlertResponse);
    });

    test("getAlert shall throw encountered error", async () => {
        (axios.get as jest.Mock).mockRejectedValue(mockError);

        try {
            await getAlert(1);
        } catch (error) {
            expect(error).toEqual(mockError);
        }
    });

    test("getAllAlerts shall invoke axios.get with appropriate input and return result", async () => {
        (axios.get as jest.Mock).mockResolvedValue({
            data: [mockAlertResponse],
        });

        const response = await getAllAlerts();

        expect(axios.get).toHaveBeenCalledWith("/api/alerts");
        expect(response).toEqual([mockAlertResponse]);
    });

    test("getAllAlerts shall throw encountered error", async () => {
        (axios.get as jest.Mock).mockRejectedValue(mockError);

        try {
            await getAllAlerts();
        } catch (error) {
            expect(error).toEqual(mockError);
        }
    });

    test("createAlert shall invoke axios.post with appropriate input and return result", async () => {
        (axios.post as jest.Mock).mockResolvedValue({
            data: [mockAlertResponse],
        });

        const response = await createAlert(mockAlertRequest);

        expect(axios.post).toHaveBeenCalledWith("/api/alerts", [
            mockAlertRequest,
        ]);
        expect(response).toEqual(mockAlertResponse);
    });

    test("createAlert shall throw encountered error", async () => {
        (axios.post as jest.Mock).mockRejectedValue(mockError);

        try {
            await createAlert(mockAlertRequest);
        } catch (error) {
            expect(error).toEqual(mockError);
        }
    });

    test("updateAlert shall invoke axios.put with appropriate input and return result", async () => {
        (axios.put as jest.Mock).mockResolvedValue({
            data: [mockAlertResponse],
        });

        const response = await updateAlert(mockAlertRequest);

        expect(axios.put).toHaveBeenCalledWith("/api/alerts", [
            mockAlertRequest,
        ]);
        expect(response).toEqual(mockAlertResponse);
    });

    test("updateAlert shall throw encountered error", async () => {
        (axios.put as jest.Mock).mockRejectedValue(mockError);

        try {
            await updateAlert(mockAlertRequest);
        } catch (error) {
            expect(error).toEqual(mockError);
        }
    });

    test("deleteAlert shall invoke axios.delete with appropriate input and return result", async () => {
        (axios.delete as jest.Mock).mockResolvedValue({
            data: mockAlertResponse,
        });

        const response = await deleteAlert(1);

        expect(axios.delete).toHaveBeenCalledWith("/api/alerts/1");
        expect(response).toEqual(mockAlertResponse);
    });

    test("deleteAlert shall throw encountered error", async () => {
        (axios.delete as jest.Mock).mockRejectedValue(mockError);

        try {
            await deleteAlert(1);
        } catch (error) {
            expect(error).toEqual(mockError);
        }
    });

    test("getAlertPreview shall invoke axios.post with appropriate input and return result", async () => {
        (axios.post as jest.Mock).mockResolvedValue({
            data: mockAlertEvaluationResponse,
        });

        const response = await getAlertPreview(mockAlertEvaluationRequest);

        expect(axios.post).toHaveBeenCalledWith(
            "/api/alerts/preview",
            mockAlertEvaluationRequest
        );
        expect(response).toEqual(mockAlertEvaluationResponse);
    });

    test("getAlertPreview shall throw encountered error", async () => {
        (axios.post as jest.Mock).mockRejectedValue(mockError);

        try {
            await getAlertPreview(mockAlertEvaluationRequest);
        } catch (error) {
            expect(error).toEqual(mockError);
        }
    });
});
