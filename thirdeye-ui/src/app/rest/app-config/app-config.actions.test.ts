import { act, renderHook } from "@testing-library/react-hooks";
import axios from "axios";
import { ActionStatus } from "../actions.interfaces";
import { useGetAppConfiguration } from "./app-config.action";

const mockAppConfigResponse = {
    clientId: "1234",
};

jest.mock("axios");
const mockedAxios = axios as jest.Mocked<typeof axios>;

describe("App Config Actions", () => {
    describe("useGetAppConfiguration", () => {
        it("should return initial default values", () => {
            const { result } = renderHook(() => useGetAppConfiguration());

            expect(result.current.appConfig).toBeNull();
            expect(result.current.getAppConfiguration).toBeDefined();
            expect(result.current.status).toEqual(ActionStatus.Initial);
            expect(result.current.errorMessages).toEqual([]);
        });

        it("should update data appropriately when making a successful REST call", async () => {
            mockedAxios.get.mockResolvedValueOnce({
                data: mockAppConfigResponse,
            });
            const { result, waitFor } = renderHook(() =>
                useGetAppConfiguration()
            );

            await act(async () => {
                const promise = result.current.getAppConfiguration();

                // Wait for state update
                await waitFor(
                    () => result.current.status === ActionStatus.Initial
                );

                // When REST call is in progress
                expect(result.current.appConfig).toBeNull();
                expect(result.current.getAppConfiguration).toBeDefined();
                expect(result.current.status).toEqual(ActionStatus.Working);
                expect(result.current.errorMessages).toEqual([]);

                return promise.then(() => {
                    // When REST call is completed
                    expect(result.current.appConfig).toEqual(
                        mockAppConfigResponse
                    );
                    expect(result.current.getAppConfiguration).toBeDefined();
                    expect(result.current.status).toEqual(ActionStatus.Done);
                    expect(result.current.errorMessages).toEqual([]);
                });
            });
        });
    });
});
