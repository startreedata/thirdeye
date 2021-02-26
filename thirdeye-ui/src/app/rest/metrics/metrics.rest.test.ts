import axios from "axios";
import { deleteMetric, getAllMetrics, getMetric } from "./metrics.rest";

jest.mock("axios");

describe("Metrics REST", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });

    test("getMetric should invoke axios.get with appropriate input and return appropriate metric", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: mockMetricResponse,
        });

        await expect(getMetric(1)).resolves.toEqual(mockMetricResponse);

        expect(axios.get).toHaveBeenCalledWith("/api/metrics/1");
    });

    test("getMetric should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getMetric(1)).rejects.toThrow("testError");
    });

    test("getAllMetrics should invoke axios.get with appropriate input and return appropriate metrics", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockMetricResponse],
        });

        await expect(getAllMetrics()).resolves.toEqual([mockMetricResponse]);

        expect(axios.get).toHaveBeenCalledWith("/api/metrics");
    });

    test("getAllMetrics should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAllMetrics()).rejects.toThrow("testError");
    });

    test("deleteMetric should invoke axios.delete with appropriate input and return appropriate metric", async () => {
        jest.spyOn(axios, "delete").mockResolvedValue({
            data: mockMetricResponse,
        });

        await expect(deleteMetric(1)).resolves.toEqual(mockMetricResponse);

        expect(axios.delete).toHaveBeenCalledWith("/api/metrics/1");
    });

    test("deleteMetric should throw encountered error", async () => {
        jest.spyOn(axios, "delete").mockRejectedValue(mockError);

        await expect(deleteMetric(1)).rejects.toThrow("testError");
    });
});

const mockMetricResponse = {
    name: "testNameMetricResponse",
};

const mockError = new Error("testError");
