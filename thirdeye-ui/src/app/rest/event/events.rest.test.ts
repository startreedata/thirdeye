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
import { Event } from "../dto/event.interfaces";
import {
    createEvent,
    createEvents,
    deleteEvent,
    getAllEvents,
    getEvent,
    getEventsForAnomaly,
    updateEvent,
    updateEvents,
} from "./events.rest";

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

        expect(axios.get).toHaveBeenCalledWith("/api/events?");
    });

    it("getAllEvents should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getAllEvents()).rejects.toThrow("testError");
    });

    it("getAllEvents with startTime should invoke axios.get with appropriate input and return appropriate events", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockEventResponse],
        });

        await expect(getAllEvents({ startTime: 1 })).resolves.toEqual([
            mockEventResponse,
        ]);

        expect(axios.get).toHaveBeenCalledWith(
            "/api/events?startTime=%5Bgte%5D1"
        );
    });

    it("getAllEvents with endTime should invoke axios.get with appropriate input and return appropriate events", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockEventResponse],
        });

        await expect(getAllEvents({ endTime: 1 })).resolves.toEqual([
            mockEventResponse,
        ]);

        expect(axios.get).toHaveBeenCalledWith(
            "/api/events?endTime=%5Blte%5D1"
        );
    });

    it("getAllEvents with type should invoke axios.get with appropriate input and return appropriate events", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockEventResponse],
        });

        await expect(getAllEvents({ type: "testType" })).resolves.toEqual([
            mockEventResponse,
        ]);

        expect(axios.get).toHaveBeenCalledWith("/api/events?type=testType");
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

    it("getEventsForAnomaly should throw encountered error", async () => {
        jest.spyOn(axios, "get").mockRejectedValue(mockError);

        await expect(getEventsForAnomaly({ anomalyId: 123 })).rejects.toThrow(
            "testError"
        );
    });

    it("getEventsForAnomaly with limit should invoke axios.get with appropriate url and return appropriate events", async () => {
        jest.spyOn(axios, "get").mockResolvedValue({
            data: [mockEventResponse],
        });

        await expect(
            getEventsForAnomaly({ anomalyId: 123, limit: 100 })
        ).resolves.toEqual([mockEventResponse]);

        expect(axios.get).toHaveBeenCalledWith(
            "/api/rca/related/events?anomalyId=123&limit=100"
        );
    });
});

const mockEventRequest = {
    name: "testNameEventRequest",
} as Event;

const mockEventResponse = {
    name: "testNameEventResponse",
};

const mockError = new Error("testError");
