import axios from "axios";
import {
    deleteAnomaly,
    getAllAnomalies,
    getAnomaliesByAlertId,
    getAnomaliesByAlertIdAndTime,
    getAnomaliesByTime,
    getAnomaly,
} from "./anomalies.rest";

jest.mock("axios");

describe("Anomalies REST", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });

    test("getAnomaly should invoke axios.get with appropriate input and return appropriate anomaly", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: mockAnomalyResponse,
        });

        await expect(getAnomaly(1)).resolves.toEqual(mockAnomalyResponse);

        expect(axios.get).toHaveBeenCalledWith("/api/anomalies/1");
    });

    test("getAnomaly should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAnomaly(1)).rejects.toThrow("testErrorMessage");
    });

    test("getAllAnomalies should invoke axios.get with appropriate input and return appropriate anomaly array", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockAnomalyResponse],
        });

        await expect(getAllAnomalies()).resolves.toEqual([mockAnomalyResponse]);

        expect(axios.get).toHaveBeenCalledWith("/api/anomalies");
    });

    test("getAllAnomalies should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAllAnomalies()).rejects.toThrow("testErrorMessage");
    });

    test("getAnomaliesByTime should invoke axios.get with appropriate input and return appropriate anomaly array", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockAnomalyResponse],
        });

        await expect(getAnomaliesByTime(1, 2)).resolves.toEqual([
            mockAnomalyResponse,
        ]);

        expect(axios.get).toHaveBeenCalledWith(
            "/api/anomalies?startTime=[gte]1&endTime=[lte]2"
        );
    });

    test("getAnomaliesByTime should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAnomaliesByTime(1, 2)).rejects.toThrow(
            "testErrorMessage"
        );
    });

    test("getAnomaliesByAlertId should invoke axios.get with appropriate input and return appropriate anomaly array", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockAnomalyResponse],
        });

        await expect(getAnomaliesByAlertId(1)).resolves.toEqual([
            mockAnomalyResponse,
        ]);

        expect(axios.get).toHaveBeenCalledWith("/api/anomalies?alert.id=1");
    });

    test("getAnomaliesByAlertId should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAnomaliesByAlertId(1)).rejects.toThrow(
            "testErrorMessage"
        );
    });

    test("getAnomaliesByAlertIdAndTime should invoke axios.get with appropriate input and return appropriate anomaly array", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockAnomalyResponse],
        });

        await expect(getAnomaliesByAlertIdAndTime(1, 2, 3)).resolves.toEqual([
            mockAnomalyResponse,
        ]);

        expect(axios.get).toHaveBeenCalledWith(
            "/api/anomalies?alert.id=1&startTime=[gte]2&endTime=[lte]3"
        );
    });

    test("getAnomaliesByAlertIdAndTime should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAnomaliesByAlertIdAndTime(1, 2, 3)).rejects.toThrow(
            "testErrorMessage"
        );
    });

    test("deleteAnomaly should invoke axios.delete with appropriate input and return appropriate anomaly", async () => {
        jest.spyOn(axios, "delete").mockResolvedValue({
            data: mockAnomalyResponse,
        });

        await expect(deleteAnomaly(1)).resolves.toEqual(mockAnomalyResponse);

        expect(axios.delete).toHaveBeenCalledWith("/api/anomalies/1");
    });

    test("deleteAnomaly should throw encountered error", async () => {
        jest.spyOn(axios, "delete").mockRejectedValue(mockError);

        await expect(deleteAnomaly(1)).rejects.toThrow("testErrorMessage");
    });
});

const mockAnomalyResponse = {
    id: 1,
};

const mockError = new Error("testErrorMessage");
