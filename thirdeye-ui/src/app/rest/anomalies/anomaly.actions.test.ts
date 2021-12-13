import { act, renderHook } from "@testing-library/react-hooks";
import axios from "axios";
import { ActionStatus } from "../actions.interfaces";
import { useGetAnomaly } from "./anomaly.actions";

const mockAnomaly = {
    id: 1,
};

jest.mock("axios");
const mockedAxios = axios as jest.Mocked<typeof axios>;

describe("Anomaly Actions", () => {
    describe("useGetAnomaly", () => {
        it("should return initial default values", () => {
            const { result } = renderHook(() => useGetAnomaly());

            expect(result.current.anomaly).toBeUndefined();
            expect(result.current.getAnomaly).toBeDefined();
            expect(result.current.status).toEqual(ActionStatus.Initial);
            expect(result.current.errorMessage).toEqual("");
        });

        it("should update data appropriately when making a successful REST call", async () => {
            mockedAxios.get.mockResolvedValueOnce({ data: mockAnomaly });
            const { result, waitFor } = renderHook(() => useGetAnomaly());
            await act(async () => {
                const promise = result.current.getAnomaly(1);

                // Wait for state update
                await waitFor(
                    () => result.current.status === ActionStatus.Initial
                );

                // When REST call is in progress
                expect(result.current.anomaly).toBeUndefined();
                expect(result.current.getAnomaly).toBeDefined();
                expect(result.current.status).toEqual(ActionStatus.Working);
                expect(result.current.errorMessage).toEqual("");

                return promise.then(() => {
                    // When REST call is completed
                    expect(result.current.anomaly).toEqual(mockAnomaly);
                    expect(result.current.getAnomaly).toBeDefined();
                    expect(result.current.status).toEqual(ActionStatus.Done);
                    expect(result.current.errorMessage).toEqual("");
                });
            });
        });
    });
});
