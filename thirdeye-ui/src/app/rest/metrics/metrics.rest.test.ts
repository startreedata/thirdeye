import axios from "axios";
import { deleteMetric, getAllMetrics, getMetric } from "./metrics.rest";

jest.mock("axios");

describe("Metrics REST", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });

    it("getMetric should invoke axios.get with appropriate input and return appropriate metric", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: mockMetric,
        });

        await expect(getMetric(1)).resolves.toEqual(mockMetric);

        expect(axios.get).toHaveBeenCalledWith("/api/metrics/1");
    });

    it("getMetric should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getMetric(1)).rejects.toThrow("testError");
    });

    it("getAllMetrics should invoke axios.get with appropriate input and return appropriate metrics", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockMetric],
        });

        await expect(getAllMetrics()).resolves.toEqual([mockMetric]);

        expect(axios.get).toHaveBeenCalledWith("/api/metrics");
    });

    it("getAllMetrics should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAllMetrics()).rejects.toThrow("testError");
    });

    it("deleteMetric should invoke axios.delete with appropriate input and return appropriate metric", async () => {
        jest.spyOn(axios, "delete").mockResolvedValue({
            data: mockMetric,
        });

        await expect(deleteMetric(1)).resolves.toEqual(mockMetric);

        expect(axios.delete).toHaveBeenCalledWith("/api/metrics/1");
    });

    it("deleteMetric should throw encountered error", async () => {
        jest.spyOn(axios, "delete").mockRejectedValue(mockError);

        await expect(deleteMetric(1)).rejects.toThrow("testError");
    });
});

const mockMetric = {
    name: "testNameMetricResponse",
};

const mockError = new Error("testError");
