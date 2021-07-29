import axios from "axios";
import { Dataset } from "../dto/dataset.interfaces";
import {
    createDataset,
    createDatasets,
    deleteDataset,
    getAllDatasets,
    getDataset,
    updateDataset,
    updateDatasets,
} from "./datasets.rest";

jest.mock("axios");

describe("Datasets REST", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });

    it("getDataset should invoke axios.get with appropriate input and return appropriate dataset", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: mockDatasetResponse,
        });

        await expect(getDataset(1)).resolves.toEqual(mockDatasetResponse);

        expect(axios.get).toHaveBeenCalledWith("/api/datasets/1");
    });

    it("getDataset should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getDataset(1)).rejects.toThrow("testError");
    });

    it("getAllDatasets should invoke axios.get with appropriate input and return appropriate datasets", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockDatasetResponse],
        });

        await expect(getAllDatasets()).resolves.toEqual([mockDatasetResponse]);

        expect(axios.get).toHaveBeenCalledWith("/api/datasets");
    });

    it("getAllDatasets should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAllDatasets()).rejects.toThrow("testError");
    });

    it("createDataset should invoke axios.post with appropriate input and return appropriate dataset", async () => {
        jest.spyOn(axios, "post").mockResolvedValue({
            data: [mockDatasetResponse],
        });

        await expect(createDataset(mockDatasetRequest)).resolves.toEqual(
            mockDatasetResponse
        );

        expect(axios.post).toHaveBeenCalledWith("/api/datasets", [
            mockDatasetRequest,
        ]);
    });

    it("createDataset should throw encountered error", async () => {
        jest.spyOn(axios, "post").mockRejectedValue(mockError);

        await expect(createDataset(mockDatasetRequest)).rejects.toThrow(
            "testError"
        );
    });

    it("createDatasets should invoke axios.post with appropriate input and return appropriate datasets", async () => {
        jest.spyOn(axios, "post").mockResolvedValue({
            data: [mockDatasetResponse],
        });

        await expect(createDatasets([mockDatasetRequest])).resolves.toEqual([
            mockDatasetResponse,
        ]);

        expect(axios.post).toHaveBeenCalledWith("/api/datasets", [
            mockDatasetRequest,
        ]);
    });

    it("createDatasets should throw encountered error", async () => {
        jest.spyOn(axios, "post").mockRejectedValue(mockError);

        await expect(createDatasets([mockDatasetRequest])).rejects.toThrow(
            "testError"
        );
    });

    it("updateDataset should invoke axios.put with appropriate input and return appropriate dataset", async () => {
        jest.spyOn(axios, "put").mockResolvedValue({
            data: [mockDatasetResponse],
        });

        await expect(updateDataset(mockDatasetRequest)).resolves.toEqual(
            mockDatasetResponse
        );

        expect(axios.put).toHaveBeenCalledWith("/api/datasets", [
            mockDatasetRequest,
        ]);
    });

    it("updateDataset should throw encountered error", async () => {
        jest.spyOn(axios, "put").mockRejectedValue(mockError);

        await expect(updateDataset(mockDatasetRequest)).rejects.toThrow(
            "testError"
        );
    });

    it("updateDatasets should invoke axios.put with appropriate input and return appropriate datasets", async () => {
        jest.spyOn(axios, "put").mockResolvedValue({
            data: [mockDatasetResponse],
        });

        await expect(updateDatasets([mockDatasetRequest])).resolves.toEqual([
            mockDatasetResponse,
        ]);

        expect(axios.put).toHaveBeenCalledWith("/api/datasets", [
            mockDatasetRequest,
        ]);
    });

    it("updateDatasets should throw encountered error", async () => {
        jest.spyOn(axios, "put").mockRejectedValue(mockError);

        await expect(updateDatasets([mockDatasetRequest])).rejects.toThrow(
            "testError"
        );
    });

    it("deleteDataset should invoke axios.delete with appropriate input and return appropriate dataset", async () => {
        jest.spyOn(axios, "delete").mockResolvedValue({
            data: mockDatasetResponse,
        });

        await expect(deleteDataset(1)).resolves.toEqual(mockDatasetResponse);

        expect(axios.delete).toHaveBeenCalledWith("/api/datasets/1");
    });

    it("deleteDataset should throw encountered error", async () => {
        jest.spyOn(axios, "delete").mockRejectedValue(mockError);

        await expect(deleteDataset(1)).rejects.toThrow("testError");
    });
});

const mockDatasetRequest = {
    name: "testNameDatasetRequest",
} as Dataset;

const mockDatasetResponse = {
    name: "testNameDatasetResponse",
};

const mockError = new Error("testError");
