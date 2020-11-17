import axios from "axios";
import { Anomaly } from "../dto/anomaly.interfaces";
import { deleteAnomaly, getAllAnomalies, getAnomaly } from "./anomaly.rest";

jest.mock("axios");

const mockAnomalyResponse: Anomaly = {
    id: 2,
} as Anomaly;

const mockError = new Error("testErrorMessage");

describe("Anomaly REST", () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    afterAll(() => {
        jest.restoreAllMocks();
    });

    test("getAnomaly shall invoke axios.get with appropriate input and return result", async () => {
        (axios.get as jest.Mock).mockResolvedValue({
            data: mockAnomalyResponse,
        });

        const response = await getAnomaly(1);

        expect(axios.get).toHaveBeenCalledWith("/api/anomalies/1");
        expect(response).toEqual(mockAnomalyResponse);
    });

    test("getAnomaly shall throw encountered error", async () => {
        (axios.get as jest.Mock).mockRejectedValue(mockError);

        await expect(getAnomaly(1)).rejects.toThrow("testErrorMessage");
    });

    test("getAllAnomalies shall invoke axios.get with appropriate input and return result", async () => {
        (axios.get as jest.Mock).mockResolvedValue({
            data: [mockAnomalyResponse],
        });

        const response = await getAllAnomalies();

        expect(axios.get).toHaveBeenCalledWith("/api/anomalies");
        expect(response).toEqual([mockAnomalyResponse]);
    });

    test("getAllAnomalies shall throw encountered error", async () => {
        (axios.get as jest.Mock).mockRejectedValue(mockError);

        await expect(getAllAnomalies()).rejects.toThrow("testErrorMessage");
    });

    test("deleteAnomaly shall invoke axios.delete with appropriate input and return result", async () => {
        (axios.delete as jest.Mock).mockResolvedValue({
            data: mockAnomalyResponse,
        });

        const response = await deleteAnomaly(1);

        expect(axios.delete).toHaveBeenCalledWith("/api/anomalies/1");
        expect(response).toEqual(mockAnomalyResponse);
    });

    test("deleteAnomaly shall throw encountered error", async () => {
        (axios.delete as jest.Mock).mockRejectedValue(mockError);

        await expect(deleteAnomaly(1)).rejects.toThrow("testErrorMessage");
    });
});
