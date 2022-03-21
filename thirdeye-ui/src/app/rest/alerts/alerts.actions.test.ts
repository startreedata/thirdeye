import { act, renderHook } from "@testing-library/react-hooks";
import axios from "axios";
import { ActionStatus } from "../actions.interfaces";
import { useGetAlert, useGetEvaluation } from "./alerts.actions";

const mockEvaluation = {
    id: 1,
};

const mockAlert = {
    alert: {
        id: 123,
    },
    start: 1637802840000,
    end: 1638591300000,
};

jest.mock("axios");
const mockedAxios = axios as jest.Mocked<typeof axios>;

describe("Alerts Actions", () => {
    describe("useGetEvaluation", () => {
        it("should return initial default values", () => {
            const { result } = renderHook(() => useGetEvaluation());

            expect(result.current.evaluation).toBeNull();
            expect(result.current.getEvaluation).toBeDefined();
            expect(result.current.status).toEqual(ActionStatus.Initial);
            expect(result.current.errorMessage).toEqual("");
        });

        it("should update data appropriately when making a successful REST call", async () => {
            mockedAxios.post.mockResolvedValueOnce({ data: mockEvaluation });
            const { result, waitFor } = renderHook(() => useGetEvaluation());

            await act(async () => {
                const promise = result.current.getEvaluation(mockAlert);

                // Wait for state update
                await waitFor(
                    () => result.current.status === ActionStatus.Initial
                );

                // When REST call is in progress
                expect(result.current.evaluation).toBeNull();
                expect(result.current.getEvaluation).toBeDefined();
                expect(result.current.status).toEqual(ActionStatus.Working);
                expect(result.current.errorMessage).toEqual("");

                return promise.then(() => {
                    // When REST call is completed
                    expect(result.current.evaluation).toEqual(mockEvaluation);
                    expect(result.current.getEvaluation).toBeDefined();
                    expect(result.current.status).toEqual(ActionStatus.Done);
                    expect(result.current.errorMessage).toEqual("");
                });
            });
        });
    });

    describe("useGetAlert", () => {
        it("should return initial default values", () => {
            const { result } = renderHook(() => useGetAlert());

            expect(result.current.alert).toBeNull();
            expect(result.current.getAlert).toBeDefined();
            expect(result.current.status).toEqual(ActionStatus.Initial);
            expect(result.current.errorMessage).toEqual("");
        });

        it("should update data appropriately when making a successful REST call", async () => {
            mockedAxios.get.mockResolvedValueOnce({
                data: { id: 123 },
            });
            const { result, waitFor } = renderHook(() => useGetAlert());

            await act(async () => {
                const promise = result.current.getAlert(123);

                // Wait for state update
                await waitFor(
                    () => result.current.status === ActionStatus.Initial
                );

                // When REST call is in progress
                expect(result.current.alert).toBeNull();
                expect(result.current.getAlert).toBeDefined();
                expect(result.current.status).toEqual(ActionStatus.Working);
                expect(result.current.errorMessage).toEqual("");

                return promise.then(() => {
                    // When REST call is completed
                    expect(result.current.alert).toEqual({ id: 123 });
                    expect(result.current.getAlert).toBeDefined();
                    expect(result.current.status).toEqual(ActionStatus.Done);
                    expect(result.current.errorMessage).toEqual("");
                });
            });
        });
    });
});
