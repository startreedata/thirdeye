import axios from "axios";
import { Anomaly } from "../dto/anomaly.interfaces";
import { deleteAnomaly, getAllAnomalies, getAnomaly } from "./anomalies-rest";

jest.mock("axios");

describe("Anomalies REST", () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    afterAll(() => {
        jest.restoreAllMocks();
    });

    test("getAnomaly should invoke axios.get with appropriate input and return result", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: mockAnomalyResponse,
        });

        expect(await getAnomaly(1)).toEqual(mockAnomalyResponse);
        expect(axios.get).toHaveBeenCalledWith("/api/anomalies/1");
    });

    test("getAnomaly should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAnomaly(1)).rejects.toThrow("testErrorMessage");
    });

    test("getAllAnomalies should invoke axios.get with appropriate input and return result", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockAnomalyResponse],
        });

        expect(await getAllAnomalies()).toEqual([mockAnomalyResponse]);
        expect(axios.get).toHaveBeenCalledWith("/api/anomalies");
    });

    test("getAllAnomalies should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAllAnomalies()).rejects.toThrow("testErrorMessage");
    });

    test("deleteAnomaly should invoke axios.delete with appropriate input and return result", async () => {
        jest.spyOn(axios, "delete").mockResolvedValue({
            data: mockAnomalyResponse,
        });

        expect(await deleteAnomaly(1)).toEqual(mockAnomalyResponse);
        expect(axios.delete).toHaveBeenCalledWith("/api/anomalies/1");
    });

    test("deleteAnomaly should throw encountered error", async () => {
        jest.spyOn(axios, "delete").mockRejectedValue(mockError);

        await expect(deleteAnomaly(1)).rejects.toThrow("testErrorMessage");
    });
});

const mockAnomalyResponse: Anomaly = {
    id: 1,
} as Anomaly;

const mockError = new Error("testErrorMessage");
