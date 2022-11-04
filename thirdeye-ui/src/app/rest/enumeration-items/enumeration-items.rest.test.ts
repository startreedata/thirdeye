import axios from "axios";
import { getEnumerationItems } from "./enumeration-items.rest";

jest.mock("axios");

describe("Enumeration Items REST", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });

    it("getEnumerationItems should invoke axios.get with appropriate input and return appropriate enumeration item", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: mockEnumerationItem,
        });

        await expect(getEnumerationItems()).resolves.toEqual(
            mockEnumerationItem
        );

        expect(axios.get).toHaveBeenCalledWith("/api/enumeration-items");
    });

    it("getEnumerationItems should invoke axios.get with appropriate query params", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [
                mockEnumerationItem,
                mockEnumerationItem,
                mockEnumerationItem,
            ],
        });

        await expect(getEnumerationItems({ ids: [1, 2, 3] })).resolves.toEqual([
            mockEnumerationItem,
            mockEnumerationItem,
            mockEnumerationItem,
        ]);

        expect(axios.get).toHaveBeenCalledWith(
            "/api/enumeration-items?id=%5Bin%5D1%2C2%2C3"
        );
    });

    it("getEnumerationItems should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getEnumerationItems()).rejects.toThrow("testError");
    });
});

const mockEnumerationItem = {
    id: 1,
};

const mockError = new Error("testError");
