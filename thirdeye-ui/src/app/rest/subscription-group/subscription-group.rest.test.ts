import axios from "axios";
import { SubscriptionGroup } from "../dto/subscription-group.interfaces";
import {
    createSubscriptionGroup,
    deleteSubscriptionGroup,
    getAllSubscriptionGroups,
    getSubscriptionGroup,
    updateSubscriptionGroup,
} from "./subscription-group.rest";

jest.mock("axios");

const mockSubscriptionGroupRequest: SubscriptionGroup = {
    name: "testSubscriptionGroupRequest",
} as SubscriptionGroup;

const mockSubscriptionGroupResponse: SubscriptionGroup = {
    name: "testSubscriptionGroupResponse",
} as SubscriptionGroup;

const mockError = {
    message: "testError",
};

describe("SubscriptionGroup REST", () => {
    beforeEach(() => {
        jest.clearAllMocks();
    });

    afterAll(() => {
        jest.restoreAllMocks();
    });

    test("getSubscriptionGroup shall invoke axios.get with appropriate input and return result", async () => {
        (axios.get as jest.Mock).mockResolvedValue({
            data: mockSubscriptionGroupResponse,
        });

        const response = await getSubscriptionGroup(1);

        expect(axios.get).toHaveBeenCalledWith("/api/subscription-groups/1");
        expect(response).toEqual(mockSubscriptionGroupResponse);
    });

    test("getSubscriptionGroup shall throw encountered error", async () => {
        (axios.get as jest.Mock).mockRejectedValue(mockError);

        try {
            await getSubscriptionGroup(1);
        } catch (error) {
            expect(error).toEqual(mockError);
        }
    });

    test("getAllSubscriptionGroups shall invoke axios.get with appropriate input and return result", async () => {
        (axios.get as jest.Mock).mockResolvedValue({
            data: [mockSubscriptionGroupResponse],
        });

        const response = await getAllSubscriptionGroups();

        expect(axios.get).toHaveBeenCalledWith("/api/subscription-groups");
        expect(response).toEqual([mockSubscriptionGroupResponse]);
    });

    test("getAllSubscriptionGroups shall throw encountered error", async () => {
        (axios.get as jest.Mock).mockRejectedValue(mockError);

        try {
            await getAllSubscriptionGroups();
        } catch (error) {
            expect(error).toEqual(mockError);
        }
    });

    test("createSubscriptionGroup shall invoke axios.post with appropriate input and return result", async () => {
        (axios.post as jest.Mock).mockResolvedValue({
            data: [mockSubscriptionGroupResponse],
        });

        const response = await createSubscriptionGroup(
            mockSubscriptionGroupRequest
        );

        expect(axios.post).toHaveBeenCalledWith("/api/subscription-groups", [
            mockSubscriptionGroupRequest,
        ]);
        expect(response).toEqual(mockSubscriptionGroupResponse);
    });

    test("createSubscriptionGroup shall throw encountered error", async () => {
        (axios.post as jest.Mock).mockRejectedValue(mockError);

        try {
            await createSubscriptionGroup(mockSubscriptionGroupRequest);
        } catch (error) {
            expect(error).toEqual(mockError);
        }
    });

    test("updateSubscriptionGroup shall invoke axios.put with appropriate input and return result", async () => {
        (axios.put as jest.Mock).mockResolvedValue({
            data: [mockSubscriptionGroupResponse],
        });

        const response = await updateSubscriptionGroup(
            mockSubscriptionGroupRequest
        );

        expect(axios.put).toHaveBeenCalledWith("/api/subscription-groups", [
            mockSubscriptionGroupRequest,
        ]);
        expect(response).toEqual(mockSubscriptionGroupResponse);
    });

    test("updateSubscriptionGroup shall throw encountered error", async () => {
        (axios.put as jest.Mock).mockRejectedValue(mockError);

        try {
            await updateSubscriptionGroup(mockSubscriptionGroupRequest);
        } catch (error) {
            expect(error).toEqual(mockError);
        }
    });

    test("deleteSubscriptionGroup shall invoke axios.delete with appropriate input and return result", async () => {
        (axios.delete as jest.Mock).mockResolvedValue({
            data: mockSubscriptionGroupResponse,
        });

        const response = await deleteSubscriptionGroup(1);

        expect(axios.delete).toHaveBeenCalledWith("/api/subscription-groups/1");
        expect(response).toEqual(mockSubscriptionGroupResponse);
    });

    test("deleteSubscriptionGroup shall throw encountered error", async () => {
        (axios.delete as jest.Mock).mockRejectedValue(mockError);

        try {
            await deleteSubscriptionGroup(1);
        } catch (error) {
            expect(error).toEqual(mockError);
        }
    });
});
