import axios from "axios";
import { getAppConfiguration } from "./app-config.rest";

jest.mock("axios");

describe("App Config REST", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });

    it("getAppConfiguration should invoke axios.get with appropriate input and return appropriate response", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: mockAppConfigResponse,
        });

        await expect(getAppConfiguration()).resolves.toEqual(
            mockAppConfigResponse
        );

        expect(axios.get).toHaveBeenCalledWith("/api/ui/config");
    });

    it("getAppConfiguration should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAppConfiguration()).rejects.toThrow("testError");
    });
});

const mockAppConfigResponse = {
    clientId: "1234",
};

const mockError = new Error("testError");
