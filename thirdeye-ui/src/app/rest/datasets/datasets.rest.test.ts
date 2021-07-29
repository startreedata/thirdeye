import axios from "axios";
import { getAllDatasets } from "./datasets.rest";

jest.mock("axios");

describe("Datasets REST", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });

    it("Datasets should invoke axios.get and return appropriate datasets", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: mockMetric,
        });

        await expect(getAllDatasets()).resolves.toEqual(mockMetric);

        expect(axios.get).toHaveBeenCalledWith("/api/datasets");
    });
});

const mockMetric = {
    name: "testNameDatasetResponse",
};
