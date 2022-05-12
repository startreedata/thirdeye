import axios from "axios";
import { Event } from "../dto/event.interfaces";
import {
    createEvent,
    createEvents,
    deleteEvent,
    getAllEvents,
    getEvent,
    updateEvent,
    updateEvents,
} from "./event.rest";

jest.mock("axios");

describe("Event REST", () => {
    afterEach(() => {
        jest.restoreAllMocks();
    });

    it("getEvent should invoke axios.get with appropriate input and return appropriate event", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: mockEventResponse,
        });

        await expect(getEvent(1)).resolves.toEqual(mockEventResponse);

        expect(axios.get).toHaveBeenCalledWith("/api/events/1");
    });

    it("getEvent should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getEvent(1)).rejects.toThrow("testError");
    });

    it("getAllEvents should invoke axios.get with appropriate input and return appropriate events", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockEventResponse],
        });

        await expect(getAllEvents()).resolves.toEqual([mockEventResponse]);

        expect(axios.get).toHaveBeenCalledWith("/api/events");
    });

    it("getAllEvents should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAllEvents()).rejects.toThrow("testError");
    });

    it("createEvent should invoke axios.post with appropriate input and return appropriate event", async () => {
        jest.spyOn(axios, "post").mockResolvedValue({
            data: [mockEventResponse],
        });

        await expect(createEvent(mockEventRequest)).resolves.toEqual(
            mockEventResponse
        );

        expect(axios.post).toHaveBeenCalledWith("/api/events", [
            mockEventRequest,
        ]);
    });

    it("createEvent should throw encountered error", async () => {
        jest.spyOn(axios, "post").mockRejectedValue(mockError);

        await expect(createEvent(mockEventRequest)).rejects.toThrow(
            "testError"
        );
    });

    it("createEvents should invoke axios.post with appropriate input and return appropriate events", async () => {
        jest.spyOn(axios, "post").mockResolvedValue({
            data: [mockEventResponse],
        });

        await expect(createEvents([mockEventRequest])).resolves.toEqual([
            mockEventResponse,
        ]);

        expect(axios.post).toHaveBeenCalledWith("/api/events", [
            mockEventRequest,
        ]);
    });

    it("createEvents should throw encountered error", async () => {
        jest.spyOn(axios, "post").mockRejectedValue(mockError);

        await expect(createEvents([mockEventRequest])).rejects.toThrow(
            "testError"
        );
    });

    it("updateEvent should invoke axios.put with appropriate input and return appropriate event", async () => {
        jest.spyOn(axios, "put").mockResolvedValue({
            data: [mockEventResponse],
        });

        await expect(updateEvent(mockEventRequest)).resolves.toEqual(
            mockEventResponse
        );

        expect(axios.put).toHaveBeenCalledWith("/api/events", [
            mockEventRequest,
        ]);
    });

    it("updateEvent should throw encountered error", async () => {
        jest.spyOn(axios, "put").mockRejectedValue(mockError);

        await expect(updateEvent(mockEventRequest)).rejects.toThrow(
            "testError"
        );
    });

    it("updateEvents should invoke axios.put with appropriate input and return appropriate events", async () => {
        jest.spyOn(axios, "put").mockResolvedValue({
            data: [mockEventResponse],
        });

        await expect(updateEvents([mockEventRequest])).resolves.toEqual([
            mockEventResponse,
        ]);

        expect(axios.put).toHaveBeenCalledWith("/api/events", [
            mockEventRequest,
        ]);
    });

    it("updateEvents should throw encountered error", async () => {
        jest.spyOn(axios, "put").mockRejectedValue(mockError);

        await expect(updateEvents([mockEventRequest])).rejects.toThrow(
            "testError"
        );
    });

    it("deleteEvent should invoke axios.delete with appropriate input and return appropriate event", async () => {
        jest.spyOn(axios, "delete").mockResolvedValue({
            data: mockEventResponse,
        });

        await expect(deleteEvent(1)).resolves.toEqual(mockEventResponse);

        expect(axios.delete).toHaveBeenCalledWith("/api/events/1");
    });

    it("deleteEvent should throw encountered error", async () => {
        jest.spyOn(axios, "delete").mockRejectedValue(mockError);

        await expect(deleteEvent(1)).rejects.toThrow("testError");
    });
});

const mockEventRequest = {
    name: "testNameEventRequest",
} as Event;

const mockEventResponse = {
    name: "testNameEventResponse",
};

const mockError = new Error("testError");
