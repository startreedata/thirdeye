import { act, renderHook } from "@testing-library/react-hooks";
import axios from "axios";
import { ActionStatus } from "../actions.interfaces";
import { useGetEvent, useGetEventsForAnomaly } from "./event.actions";

const mockEvent = {
    id: 1,
};

jest.mock("axios");
const mockedAxios = axios as jest.Mocked<typeof axios>;

describe("Event Actions", () => {
    describe("useGetEvent", () => {
        it("should return initial default values", () => {
            const { result } = renderHook(() => useGetEvent());

            expect(result.current.event).toBeNull();
            expect(result.current.event).toBeDefined();
            expect(result.current.status).toEqual(ActionStatus.Initial);
            expect(result.current.errorMessages).toEqual([]);
        });

        it("should update data appropriately when making a successful REST call", async () => {
            mockedAxios.get.mockResolvedValueOnce({ data: mockEvent });
            const { result, waitFor } = renderHook(() => useGetEvent());
            await act(async () => {
                const promise = result.current.getEvent(1);

                // Wait for state update
                await waitFor(
                    () => result.current.status === ActionStatus.Initial
                );

                // When REST call is in progress
                expect(result.current.event).toBeNull();
                expect(result.current.getEvent).toBeDefined();
                expect(result.current.status).toEqual(ActionStatus.Working);
                expect(result.current.errorMessages).toEqual([]);

                return promise.then(() => {
                    // When REST call is completed
                    expect(result.current.event).toEqual(mockEvent);
                    expect(result.current.getEvent).toBeDefined();
                    expect(result.current.status).toEqual(ActionStatus.Done);
                    expect(result.current.errorMessages).toEqual([]);
                });
            });
        });
    });

    describe("useGetEventsForAnomaly", () => {
        it("should return initial default values", () => {
            const { result } = renderHook(() => useGetEventsForAnomaly());

            expect(result.current.events).toBeNull();
            expect(result.current.getEventsForAnomaly).toBeDefined();
            expect(result.current.status).toEqual(ActionStatus.Initial);
            expect(result.current.errorMessages).toEqual([]);
        });

        it("should update data appropriately when making a successful REST call", async () => {
            mockedAxios.get.mockResolvedValueOnce({ data: [mockEvent] });
            const { result, waitFor } = renderHook(() =>
                useGetEventsForAnomaly()
            );
            await act(async () => {
                const promise = result.current.getEventsForAnomaly({
                    anomalyId: 1,
                });

                // Wait for state update
                await waitFor(
                    () => result.current.status === ActionStatus.Initial
                );

                // When REST call is in progress
                expect(result.current.events).toBeNull();
                expect(result.current.getEventsForAnomaly).toBeDefined();
                expect(result.current.status).toEqual(ActionStatus.Working);
                expect(result.current.errorMessages).toEqual([]);

                return promise.then(() => {
                    // When REST call is completed
                    expect(result.current.events).toEqual([mockEvent]);
                    expect(result.current.getEventsForAnomaly).toBeDefined();
                    expect(result.current.status).toEqual(ActionStatus.Done);
                    expect(result.current.errorMessages).toEqual([]);
                });
            });
        });
    });
});
