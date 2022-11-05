import axios from "axios";
import { LogicalMetric } from "../dto/metric.interfaces";
import {
    createMetric,
    deleteMetric,
    getAllMetrics,
    getMetric,
    updateMetric,
} from "./metrics.rest";

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

    it("createMetric should invoke axios.post with appropriate input and return the created metric", async () => {
        jest.spyOn(axios, "post").mockResolvedValue({
            data: [mockMetricResponse],
        });

        await expect(createMetric(mockMetricRequest)).resolves.toEqual(
            mockMetricResponse
        );

        expect(axios.post).toHaveBeenCalledWith("/api/metrics", [
            mockMetricRequest,
        ]);
    });

    it("createMetric should throw encountered error", async () => {
        jest.spyOn(axios, "post").mockRejectedValue(mockError);

        await expect(createMetric(mockMetricRequest)).rejects.toThrow(
            "testError"
        );
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

    it("getAllMetrics should invoke axios.get with appropriate params and return appropriate metrics", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockMetric],
        });

        await expect(getAllMetrics({ datasetId: 123 })).resolves.toEqual([
            mockMetric,
        ]);

        expect(axios.get).toHaveBeenCalledWith("/api/metrics?dataset.id=123");
    });

    it("getAllMetrics should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAllMetrics()).rejects.toThrow("testError");
    });

    it("updateMetric should invoke axios.put with appropriate input and return appropriate metric", async () => {
        jest.spyOn(axios, "put").mockResolvedValue({
            data: [mockMetricResponse],
        });

        await expect(updateMetric(mockMetricRequest)).resolves.toEqual(
            mockMetricResponse
        );

        expect(axios.put).toHaveBeenCalledWith("/api/metrics", [
            mockMetricRequest,
        ]);
    });

    it("updateMetric should throw encountered error", async () => {
        jest.spyOn(axios, "put").mockRejectedValue(mockError);

        await expect(updateMetric(mockMetricRequest)).rejects.toThrow(
            "testError"
        );
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

const mockMetricResponse = {
    name: "testNameMetricResponse",
};

const mockMetricRequest = {
    name: "testNameMetricRequest",
} as LogicalMetric;

const mockMetric = {
    name: "testNameMetricResponse",
};

const mockError = new Error("testError");
