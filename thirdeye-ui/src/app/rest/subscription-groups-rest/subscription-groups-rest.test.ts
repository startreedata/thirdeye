import axios from "axios";
import { SubscriptionGroup } from "../dto/subscription-group.interfaces";
import {
    createSubscriptionGroup,
    deleteSubscriptionGroup,
    getAllSubscriptionGroups,
    getSubscriptionGroup,
    updateSubscriptionGroup,
} from "./subscription-groups-rest";

jest.mock("axios");

describe("Subscription Groups REST", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });

    test("getSubscriptionGroup should invoke axios.get with appropriate input and return appropriate subscription group", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: mockSubscriptionGroupResponse,
        });

        expect(await getSubscriptionGroup(1)).toEqual(
            mockSubscriptionGroupResponse
        );
        expect(axios.get).toHaveBeenCalledWith("/api/subscription-groups/1");
    });

    test("getSubscriptionGroup should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getSubscriptionGroup(1)).rejects.toThrow(
            "testErrorMessage"
        );
    });

    test("getAllSubscriptionGroups should invoke axios.get with appropriate input and return appropriate subscription group array", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockSubscriptionGroupResponse],
        });

        expect(await getAllSubscriptionGroups()).toEqual([
            mockSubscriptionGroupResponse,
        ]);
        expect(axios.get).toHaveBeenCalledWith("/api/subscription-groups");
    });

    test("getAllSubscriptionGroups should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAllSubscriptionGroups()).rejects.toThrow(
            "testErrorMessage"
        );
    });

    test("createSubscriptionGroup should invoke axios.post with appropriate input and return appropriate subscription group", async () => {
        jest.spyOn(axios, "post").mockResolvedValue({
            data: [mockSubscriptionGroupResponse],
        });

        expect(
            await createSubscriptionGroup(mockSubscriptionGroupRequest)
        ).toEqual(mockSubscriptionGroupResponse);
        expect(axios.post).toHaveBeenCalledWith("/api/subscription-groups", [
            mockSubscriptionGroupRequest,
        ]);
    });

    test("createSubscriptionGroup should throw encountered error", async () => {
        jest.spyOn(axios, "post").mockRejectedValue(mockError);

        await expect(
            createSubscriptionGroup(mockSubscriptionGroupRequest)
        ).rejects.toThrow("testErrorMessage");
    });

    test("updateSubscriptionGroup should invoke axios.put with appropriate input and return appropriate subscription group", async () => {
        jest.spyOn(axios, "put").mockResolvedValue({
            data: [mockSubscriptionGroupResponse],
        });

        expect(
            await updateSubscriptionGroup(mockSubscriptionGroupRequest)
        ).toEqual(mockSubscriptionGroupResponse);
        expect(axios.put).toHaveBeenCalledWith("/api/subscription-groups", [
            mockSubscriptionGroupRequest,
        ]);
    });

    test("updateSubscriptionGroup should throw encountered error", async () => {
        jest.spyOn(axios, "put").mockRejectedValue(mockError);

        await expect(
            updateSubscriptionGroup(mockSubscriptionGroupRequest)
        ).rejects.toThrow("testErrorMessage");
    });

    test("deleteSubscriptionGroup should invoke axios.delete with appropriate input and return appropriate subscription group", async () => {
        jest.spyOn(axios, "delete").mockResolvedValue({
            data: mockSubscriptionGroupResponse,
        });

        expect(await deleteSubscriptionGroup(1)).toEqual(
            mockSubscriptionGroupResponse
        );
        expect(axios.delete).toHaveBeenCalledWith("/api/subscription-groups/1");
    });

    test("deleteSubscriptionGroup should throw encountered error", async () => {
        jest.spyOn(axios, "delete").mockRejectedValue(mockError);

        await expect(deleteSubscriptionGroup(1)).rejects.toThrow(
            "testErrorMessage"
        );
    });
});

const mockSubscriptionGroupRequest: SubscriptionGroup = {
    name: "testSubscriptionGroupNameRequest",
} as SubscriptionGroup;

const mockSubscriptionGroupResponse: SubscriptionGroup = {
    name: "testSubscriptionGroupNameResponse",
} as SubscriptionGroup;

const mockError = new Error("testErrorMessage");
