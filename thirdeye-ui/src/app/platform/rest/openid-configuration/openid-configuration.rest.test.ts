import axios from "axios";
import { getOpenIDConfigurationV1 } from "./openid-configuration.rest";

jest.mock("axios");

describe("OpenID Configuration REST", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });

    it("getOpenIDConfigurationV1 should invoke axios.get with appropriate input and return appropriate OpenID configuration", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: mockOpenIDConfigurationV1Response,
        });

        await expect(
            getOpenIDConfigurationV1("testOidcIssuerUrl")
        ).resolves.toEqual(mockOpenIDConfigurationV1Response);

        expect(axios.get).toHaveBeenCalledWith(
            "testOidcIssuerUrl/.well-known/openid-configuration"
        );
    });

    it("getOpenIDConfigurationV1 should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(
            getOpenIDConfigurationV1("testOidcIssuerUrl")
        ).rejects.toThrow("testError");
    });
});

const mockOpenIDConfigurationV1Response = {
    issuer: "testIssuer",
};

const mockError = new Error("testError");
