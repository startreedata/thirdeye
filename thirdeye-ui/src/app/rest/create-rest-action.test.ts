import { act, renderHook } from "@testing-library/react-hooks";
import axios from "axios";
import { ActionStatus } from "./actions.interfaces";
import { useHTTPAction } from "./create-rest-action";

const mockError = {
    response: {
        data: {
            message: "testError",
        },
    },
};

describe("Create Rest Action (useHTTPAction)", () => {
    it("should return initial default values", () => {
        const { result } = renderHook(() => useHTTPAction(axios.get));

        expect(result.current.data).toBeNull();
        expect(result.current.makeRequest).toBeDefined();
        expect(result.current.status).toEqual(ActionStatus.Initial);
        expect(result.current.errorMessage).toEqual("");
    });

    it("should invoke passed fetch function when calling makeRequest", async () => {
        const mockFetchFunction = jest.fn();
        const { result } = renderHook(() => useHTTPAction(mockFetchFunction));
        await act(async () => {
            await result.current.makeRequest(
                "/api/hello/world",
                "another",
                "param"
            );
        });

        expect(mockFetchFunction).toHaveBeenCalledWith(
            "/api/hello/world",
            "another",
            "param"
        );
    });

    it("should update data appropriately when making a successful REST call when invoking makeRequest", async () => {
        const mockFetchFunction = jest.fn();
        const mockResponse = {
            data: {
                id: 123,
            },
        };
        const { result, waitFor } = renderHook(() =>
            useHTTPAction(mockFetchFunction)
        );

        mockFetchFunction.mockResolvedValue(mockResponse);

        await act(async () => {
            const promise = result.current.makeRequest("/api/hello/world");

            // Wait for state update
            await waitFor(() => result.current.status === ActionStatus.Initial);

            // When REST call is in progress
            expect(result.current.data).toBeNull();
            expect(result.current.makeRequest).toBeDefined();
            expect(result.current.status).toEqual(ActionStatus.Working);
            expect(result.current.errorMessage).toEqual("");

            return promise.then(() => {
                // When REST call is completed
                expect(result.current.data).toEqual(mockResponse);
                expect(result.current.makeRequest).toBeDefined();
                expect(result.current.status).toEqual(ActionStatus.Done);
                expect(result.current.errorMessage).toEqual("");
            });
        });
    });

    it("should update data appropriately when making an unsuccessful REST call when invoking makeRequest", async () => {
        const mockFetchFunction = jest.fn();
        const { result, waitFor } = renderHook(() =>
            useHTTPAction(mockFetchFunction)
        );

        mockFetchFunction.mockRejectedValue(mockError);

        await act(async () => {
            const promise = result.current.makeRequest("/api/hello/world");

            // Wait for state update
            await waitFor(() => result.current.status === ActionStatus.Initial);

            // When REST call is in progress
            expect(result.current.data).toBeNull();
            expect(result.current.makeRequest).toBeDefined();
            expect(result.current.status).toEqual(ActionStatus.Working);
            expect(result.current.errorMessage).toEqual("");

            return promise.then(() => {
                // When REST call is completed
                expect(result.current.data).toBeNull();
                expect(result.current.makeRequest).toBeDefined();
                expect(result.current.status).toEqual(ActionStatus.Error);
                expect(result.current.errorMessage).toEqual("testError");
            });
        });
    });
});
