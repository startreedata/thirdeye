// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
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
