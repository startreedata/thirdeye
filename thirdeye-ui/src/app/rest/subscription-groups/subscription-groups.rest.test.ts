// /
// / Copyright 2022 StarTree Inc
// /
// / Licensed under the StarTree Community License (the "License"); you may not use
// / this file except in compliance with the License. You may obtain a copy of the
// / License at http://www.startree.ai/legal/startree-community-license
// /
// / Unless required by applicable law or agreed to in writing, software distributed under the
// / License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// / either express or implied.
// / See the License for the specific language governing permissions and limitations under
// / the License.
// /

import axios from "axios";
import { SubscriptionGroup } from "../dto/subscription-group.interfaces";
import {
    createSubscriptionGroup,
    createSubscriptionGroups,
    deleteSubscriptionGroup,
    getAllSubscriptionGroups,
    getSubscriptionGroup,
    updateSubscriptionGroup,
    updateSubscriptionGroups,
} from "./subscription-groups.rest";

jest.mock("axios");

describe("Subscription Groups REST", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });

    it("getSubscriptionGroup should invoke axios.get with appropriate input and return appropriate subscription group", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: mockSubscriptionGroupResponse,
        });

        await expect(getSubscriptionGroup(1)).resolves.toEqual(
            mockSubscriptionGroupResponse
        );

        expect(axios.get).toHaveBeenCalledWith("/api/subscription-groups/1");
    });

    it("getSubscriptionGroup should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getSubscriptionGroup(1)).rejects.toThrow("testError");
    });

    it("getAllSubscriptionGroups should invoke axios.get with appropriate input and return appropriate subscription groups", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockSubscriptionGroupResponse],
        });

        await expect(getAllSubscriptionGroups()).resolves.toEqual([
            mockSubscriptionGroupResponse,
        ]);

        expect(axios.get).toHaveBeenCalledWith("/api/subscription-groups");
    });

    it("getAllSubscriptionGroups should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAllSubscriptionGroups()).rejects.toThrow("testError");
    });

    it("createSubscriptionGroup should invoke axios.post with appropriate input and return appropriate subscription group", async () => {
        jest.spyOn(axios, "post").mockResolvedValue({
            data: [mockSubscriptionGroupResponse],
        });

        await expect(
            createSubscriptionGroup(mockSubscriptionGroupRequest)
        ).resolves.toEqual(mockSubscriptionGroupResponse);

        expect(axios.post).toHaveBeenCalledWith("/api/subscription-groups", [
            mockSubscriptionGroupRequest,
        ]);
    });

    it("createSubscriptionGroup should throw encountered error", async () => {
        jest.spyOn(axios, "post").mockRejectedValue(mockError);

        await expect(
            createSubscriptionGroup(mockSubscriptionGroupRequest)
        ).rejects.toThrow("testError");
    });

    it("createSubscriptionGroups should invoke axios.post with appropriate input and return appropriate subscription groups", async () => {
        jest.spyOn(axios, "post").mockResolvedValue({
            data: [mockSubscriptionGroupResponse],
        });

        await expect(
            createSubscriptionGroups([mockSubscriptionGroupRequest])
        ).resolves.toEqual([mockSubscriptionGroupResponse]);

        expect(axios.post).toHaveBeenCalledWith("/api/subscription-groups", [
            mockSubscriptionGroupRequest,
        ]);
    });

    it("createSubscriptionGroups should throw encountered error", async () => {
        jest.spyOn(axios, "post").mockRejectedValue(mockError);

        await expect(
            createSubscriptionGroups([mockSubscriptionGroupRequest])
        ).rejects.toThrow("testError");
    });

    it("updateSubscriptionGroup should invoke axios.put with appropriate input and return appropriate subscription group", async () => {
        jest.spyOn(axios, "put").mockResolvedValue({
            data: [mockSubscriptionGroupResponse],
        });

        await expect(
            updateSubscriptionGroup(mockSubscriptionGroupRequest)
        ).resolves.toEqual(mockSubscriptionGroupResponse);

        expect(axios.put).toHaveBeenCalledWith("/api/subscription-groups", [
            mockSubscriptionGroupRequest,
        ]);
    });

    it("updateSubscriptionGroup should throw encountered error", async () => {
        jest.spyOn(axios, "put").mockRejectedValue(mockError);

        await expect(
            updateSubscriptionGroup(mockSubscriptionGroupRequest)
        ).rejects.toThrow("testError");
    });

    it("updateSubscriptionGroups should invoke axios.put with appropriate input and return appropriate subscription groups", async () => {
        jest.spyOn(axios, "put").mockResolvedValue({
            data: [mockSubscriptionGroupResponse],
        });

        await expect(
            updateSubscriptionGroups([mockSubscriptionGroupRequest])
        ).resolves.toEqual([mockSubscriptionGroupResponse]);

        expect(axios.put).toHaveBeenCalledWith("/api/subscription-groups", [
            mockSubscriptionGroupRequest,
        ]);
    });

    it("updateSubscriptionGroups should throw encountered error", async () => {
        jest.spyOn(axios, "put").mockRejectedValue(mockError);

        await expect(
            updateSubscriptionGroups([mockSubscriptionGroupRequest])
        ).rejects.toThrow("testError");
    });

    it("deleteSubscriptionGroup should invoke axios.delete with appropriate input and return appropriate subscription group", async () => {
        jest.spyOn(axios, "delete").mockResolvedValue({
            data: mockSubscriptionGroupResponse,
        });

        await expect(deleteSubscriptionGroup(1)).resolves.toEqual(
            mockSubscriptionGroupResponse
        );

        expect(axios.delete).toHaveBeenCalledWith("/api/subscription-groups/1");
    });

    it("deleteSubscriptionGroup should throw encountered error", async () => {
        jest.spyOn(axios, "delete").mockRejectedValue(mockError);

        await expect(deleteSubscriptionGroup(1)).rejects.toThrow("testError");
    });
});

const mockSubscriptionGroupRequest = {
    name: "testNameSubscriptionGroupRequest",
} as SubscriptionGroup;

const mockSubscriptionGroupResponse = {
    name: "testNameSubscriptionGroupResponse",
};

const mockError = new Error("testError");
