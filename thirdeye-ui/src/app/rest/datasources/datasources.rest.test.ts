import axios from "axios";
import { Dataset } from "../dto/dataset.interfaces";
import { Datasource } from "../dto/datasource.interfaces";
import {
    createDatasource,
    createDatasources,
    deleteDatasource,
    getAllDatasources,
    getDatasource,
    getStatusForDatasource,
    onboardAllDatasets,
    updateDatasource,
    updateDatasources,
} from "./datasources.rest";

jest.mock("axios");

describe("Datasources REST", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });

    it("getDatasource should invoke axios.get with appropriate input and return appropriate datasource", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: mockDatasourceResponse,
        });

        await expect(getDatasource(1)).resolves.toEqual(mockDatasourceResponse);

        expect(axios.get).toHaveBeenCalledWith("/api/data-sources/1");
    });

    it("getDatasource should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getDatasource(1)).rejects.toThrow("testError");
    });

    it("getAllDatasources should invoke axios.get with appropriate input and return appropriate datasources", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockDatasourceResponse],
        });

        await expect(getAllDatasources()).resolves.toEqual([
            mockDatasourceResponse,
        ]);

        expect(axios.get).toHaveBeenCalledWith("/api/data-sources");
    });

    it("getAllDatasources should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAllDatasources()).rejects.toThrow("testError");
    });

    it("createDatasource should invoke axios.post with appropriate input and return appropriate datasource", async () => {
        jest.spyOn(axios, "post").mockResolvedValue({
            data: [mockDatasourceResponse],
        });

        await expect(createDatasource(mockDatasourceRequest)).resolves.toEqual(
            mockDatasourceResponse
        );

        expect(axios.post).toHaveBeenCalledWith("/api/data-sources", [
            mockDatasourceRequest,
        ]);
    });

    it("createDatasource should throw encountered error", async () => {
        jest.spyOn(axios, "post").mockRejectedValue(mockError);

        await expect(createDatasource(mockDatasourceRequest)).rejects.toThrow(
            "testError"
        );
    });

    it("createDatasources should invoke axios.post with appropriate input and return appropriate datasources", async () => {
        jest.spyOn(axios, "post").mockResolvedValue({
            data: [mockDatasourceResponse],
        });

        await expect(
            createDatasources([mockDatasourceRequest])
        ).resolves.toEqual([mockDatasourceResponse]);

        expect(axios.post).toHaveBeenCalledWith("/api/data-sources", [
            mockDatasourceRequest,
        ]);
    });

    it("createDatasources should throw encountered error", async () => {
        jest.spyOn(axios, "post").mockRejectedValue(mockError);

        await expect(
            createDatasources([mockDatasourceRequest])
        ).rejects.toThrow("testError");
    });

    it("updateDatasource should invoke axios.put with appropriate input and return appropriate datasource", async () => {
        jest.spyOn(axios, "put").mockResolvedValue({
            data: [mockDatasourceResponse],
        });

        await expect(updateDatasource(mockDatasourceRequest)).resolves.toEqual(
            mockDatasourceResponse
        );

        expect(axios.put).toHaveBeenCalledWith("/api/data-sources", [
            mockDatasourceRequest,
        ]);
    });

    it("updateDatasource should throw encountered error", async () => {
        jest.spyOn(axios, "put").mockRejectedValue(mockError);

        await expect(updateDatasource(mockDatasourceRequest)).rejects.toThrow(
            "testError"
        );
    });

    it("updateDatasources should invoke axios.put with appropriate input and return appropriate datasources", async () => {
        jest.spyOn(axios, "put").mockResolvedValue({
            data: [mockDatasourceResponse],
        });

        await expect(
            updateDatasources([mockDatasourceRequest])
        ).resolves.toEqual([mockDatasourceResponse]);

        expect(axios.put).toHaveBeenCalledWith("/api/data-sources", [
            mockDatasourceRequest,
        ]);
    });

    it("updateDatasources should throw encountered error", async () => {
        jest.spyOn(axios, "put").mockRejectedValue(mockError);

        await expect(
            updateDatasources([mockDatasourceRequest])
        ).rejects.toThrow("testError");
    });

    it("onboardAllDatasets should invoke axios.post with appropriate input and return appropriate datasets", async () => {
        jest.spyOn(axios, "post").mockResolvedValue({
            data: [mockDatasetsResponse],
        });

        await expect(
            onboardAllDatasets(mockDatasourceRequest.name)
        ).resolves.toEqual([mockDatasetsResponse]);

        expect(axios.post).toHaveBeenCalledWith(
            "/api/data-sources/onboard-all",
            new URLSearchParams({ name: mockDatasourceRequest.name })
        );
    });

    it("onboardAllDatasets should throw encountered error", async () => {
        jest.spyOn(axios, "post").mockRejectedValue(mockError);

        await expect(
            onboardAllDatasets(mockDatasourceRequest.name)
        ).rejects.toThrow("testError");
    });

    it("deleteDatasource should invoke axios.delete with appropriate input and return appropriate datasource", async () => {
        jest.spyOn(axios, "delete").mockResolvedValue({
            data: mockDatasourceResponse,
        });

        await expect(deleteDatasource(1)).resolves.toEqual(
            mockDatasourceResponse
        );

        expect(axios.delete).toHaveBeenCalledWith("/api/data-sources/1");
    });

    it("deleteDatasource should throw encountered error", async () => {
        jest.spyOn(axios, "delete").mockRejectedValue(mockError);

        await expect(deleteDatasource(1)).rejects.toThrow("testError");
    });

    it("getStatusForDatasource should invoke axios.get with appropriate input and return appropriate status", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: mockStatusResponse,
        });

        await expect(
            getStatusForDatasource("datasource-name")
        ).resolves.toEqual(mockStatusResponse);

        expect(axios.get).toHaveBeenCalledWith(
            "/api/data-sources/validate?name=datasource-name"
        );
    });

    it("getStatusForDatasource should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getStatusForDatasource("datasource-name")).rejects.toThrow(
            "testError"
        );
    });
});

const mockDatasourceRequest = {
    name: "testNameDatasourceRequest",
} as Datasource;

const mockDatasetsResponse = {
    name: "testNameDatasetResponse",
} as Dataset;

const mockDatasourceResponse = {
    name: "testNameDatasourceResponse",
};
const mockStatusResponse = {
    code: "HEALTHY",
};

const mockError = new Error("testError");
