import axios from "axios";
import { getInfoV1 } from "./info.rest";

jest.mock("axios");

describe("Info REST", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });

    it("getInfoV1 should invoke axios.get with appropriate input and return appropriate info", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: mockInfoV1Response,
        });

        await expect(getInfoV1()).resolves.toEqual(mockInfoV1Response);

        expect(axios.get).toHaveBeenCalledWith("/api/info");
    });

    it("getInfoV1 should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getInfoV1()).rejects.toThrow("testError");
    });
});

const mockInfoV1Response = {
    oidcIssuerUrl: "testOidcIssuerUrl",
};

const mockError = new Error("testError");
