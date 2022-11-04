import { act, renderHook } from "@testing-library/react-hooks";
import axios from "axios";
import { ActionStatus } from "../actions.interfaces";
import { useGetTasks } from "./tasks.actions";

const mockAnomaly = {
    id: 1,
};

jest.mock("axios");
const mockedAxios = axios as jest.Mocked<typeof axios>;

describe("Tasks Actions", () => {
    describe("useGetTasks", () => {
        it("should return initial default values", () => {
            const { result } = renderHook(() => useGetTasks());

            expect(result.current.tasks).toBeNull();
            expect(result.current.getTasks).toBeDefined();
            expect(result.current.status).toEqual(ActionStatus.Initial);
            expect(result.current.errorMessages).toEqual([]);
        });

        it("should update data appropriately when making a successful REST call", async () => {
            mockedAxios.get.mockResolvedValueOnce({ data: mockAnomaly });
            const { result, waitFor } = renderHook(() => useGetTasks());
            await act(async () => {
                const promise = result.current.getTasks({});

                // Wait for state update
                await waitFor(
                    () => result.current.status === ActionStatus.Initial
                );

                // When REST call is in progress
                expect(result.current.tasks).toBeNull();
                expect(result.current.getTasks).toBeDefined();
                expect(result.current.status).toEqual(ActionStatus.Working);
                expect(result.current.errorMessages).toEqual([]);

                return promise.then(() => {
                    // When REST call is completed
                    expect(result.current.tasks).toEqual(mockAnomaly);
                    expect(result.current.getTasks).toBeDefined();
                    expect(result.current.status).toEqual(ActionStatus.Done);
                    expect(result.current.errorMessages).toEqual([]);
                });
            });
        });
    });
});
