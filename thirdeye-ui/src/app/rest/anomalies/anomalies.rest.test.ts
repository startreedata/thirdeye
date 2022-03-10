import axios from "axios";
import {
    AnomalyFeedback,
    AnomalyFeedbackType,
} from "../dto/anomaly.interfaces";
import {
    deleteAnomaly,
    getAllAnomalies,
    getAnomaliesByAlertId,
    getAnomaliesByAlertIdAndTime,
    getAnomaliesByTime,
    getAnomaly,
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

    it("getAllAnomalies should invoke axios.get with appropriate input and return appropriate anomalies", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockAnomaly],
        });

        await expect(getAllAnomalies()).resolves.toEqual([mockAnomaly]);

        expect(axios.get).toHaveBeenCalledWith("/api/anomalies?isChild=false");
    });

    it("getAllAnomalies should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAllAnomalies()).rejects.toThrow("testError");
    });

    it("getAnomaliesByAlertId should invoke axios.get with appropriate input and return appropriate anomalies", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockAnomaly],
        });

        await expect(getAnomaliesByAlertId(1)).resolves.toEqual([mockAnomaly]);

        expect(axios.get).toHaveBeenCalledWith(
            "/api/anomalies?isChild=false&alert.id=1"
        );
    });

    it("getAnomaliesByAlertId should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAnomaliesByAlertId(1)).rejects.toThrow("testError");
    });

    it("getAnomaliesByTime should invoke axios.get with appropriate input and return appropriate anomalies", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockAnomaly],
        });

        await expect(getAnomaliesByTime(1, 2)).resolves.toEqual([mockAnomaly]);

        expect(axios.get).toHaveBeenCalledWith(
            "/api/anomalies?isChild=false&startTime=[gte]1&endTime=[lte]2"
        );
    });

    it("getAnomaliesByTime should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAnomaliesByTime(1, 2)).rejects.toThrow("testError");
    });

    it("getAnomaliesByAlertIdAndTime should invoke axios.get with appropriate input and return appropriate anomalies", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockAnomaly],
        });

        await expect(getAnomaliesByAlertIdAndTime(1, 2, 3)).resolves.toEqual([
            mockAnomaly,
        ]);

        expect(axios.get).toHaveBeenCalledWith(
            "/api/anomalies?isChild=false&alert.id=1&startTime=[gte]2&endTime=[lte]3"
        );
    });

    it("getAnomaliesByAlertIdAndTime should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAnomaliesByAlertIdAndTime(1, 2, 3)).rejects.toThrow(
            "testError"
        );
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

const mockError = new Error("testError");

const mockAnomalyFeedback = {
    type: AnomalyFeedbackType.ANOMALY,
    comment: "hello world",
} as AnomalyFeedback;
