import { act, renderHook } from "@testing-library/react-hooks";
import { ActionStatus } from "../actions.interfaces";
import { useGetAnomaly } from "./anomaly.actions";

jest.mock("./anomalies.rest", () => ({
    getAnomaly: (mockGetAnomaly = jest.fn()),
}));

describe("Anomaly Actions", () => {
    it("useGetAnomaly should return initial default values", () => {
        const { result } = renderHook(() => useGetAnomaly());

        expect(result.current.anomaly).toBeUndefined();
        expect(result.current.getAnomaly).toBeDefined();
        expect(result.current.status).toEqual(ActionStatus.Initial);
        expect(result.current.errorMessage).toEqual("");
    });

    it("getMyApps should invoke appropriate REST call", async () => {
        const { result } = renderHook(() => useGetAnomaly());
        await act(async () => {
            await result.current.getAnomaly(1);
        });

        expect(mockGetAnomaly).toHaveBeenCalled();
    });

    it("useGetAnomaly should update data appropriately when making a successful REST call", async () => {
        mockGetAnomaly.mockResolvedValue(mockAnomaly);
        const { result, waitFor } = renderHook(() => useGetAnomaly());
        await act(async () => {
            const promise = result.current.getAnomaly(1);

            // Wait for state update
            await waitFor(() => result.current.status === ActionStatus.Initial);

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

    it("useGetAnomaly should update data appropriately when making an unsuccessful REST call", async () => {
        mockGetAnomaly.mockRejectedValue(mockError);
        const { result, waitFor } = renderHook(() => useGetAnomaly());
        await act(async () => {
            const promise = result.current.getAnomaly(1);

            // Wait for state update
            await waitFor(() => result.current.status === ActionStatus.Initial);

            // When REST call is in progress
            expect(result.current.anomaly).toBeUndefined();
            expect(result.current.getAnomaly).toBeDefined();
            expect(result.current.status).toEqual(ActionStatus.Working);
            expect(result.current.errorMessage).toEqual("");

            return promise.then(() => {
                // When REST call is completed
                expect(result.current.anomaly).toBeUndefined();
                expect(result.current.getAnomaly).toBeDefined();
                expect(result.current.status).toEqual(ActionStatus.Error);
                expect(result.current.errorMessage).toEqual("testError");
            });
        });
    });
});

let mockGetAnomaly: jest.Mock;

const mockAnomaly = {
    id: 1,
};

const mockError = {
    response: {
        data: {
            message: "testError",
        },
    },
};
