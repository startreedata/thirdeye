/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
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
