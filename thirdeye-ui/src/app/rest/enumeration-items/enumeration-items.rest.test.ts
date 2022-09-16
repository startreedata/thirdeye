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
            "/api/enumeration-items?id=1%2C2%2C3"
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
