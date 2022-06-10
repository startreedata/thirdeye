///
/// Copyright 2022 StarTree Inc
///
/// Licensed under the StarTree Community License (the "License"); you may not use
/// this file except in compliance with the License. You may obtain a copy of the
/// License at http://www.startree.ai/legal/startree-community-license
///
/// Unless required by applicable law or agreed to in writing, software distributed under the
/// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
/// either express or implied.
/// See the License for the specific language governing permissions and limitations under
/// the License.
///


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
