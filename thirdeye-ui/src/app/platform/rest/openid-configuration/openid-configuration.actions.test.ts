import { act, renderHook } from "@testing-library/react-hooks";
import { ActionStatus } from "../actions.interfaces";
import { useGetOpenIDConfigurationV1 } from "./openid-configuration.actions";

jest.mock("./openid-configuration.rest", () => ({
    getOpenIDConfigurationV1: (mockGetOpenIDConfigurationV1 = jest.fn()),
}));

describe("OpenID Configuration Actions", () => {
    it("useGetOpenIDConfigurationV1 should return initial default values", () => {
        const { result } = renderHook(() => useGetOpenIDConfigurationV1());

        expect(result.current.openIDConfigurationV1).toBeNull();
        expect(result.current.getOpenIDConfigurationV1).toBeDefined();
        expect(result.current.status).toEqual(ActionStatus.Initial);
        expect(result.current.errorMessage).toEqual("");
    });

    it("getOpenIDConfigurationV1 should invoke appropriate REST call", async () => {
        const { result } = renderHook(() => useGetOpenIDConfigurationV1());
        await act(async () => {
            await result.current.getOpenIDConfigurationV1("testOidcIssuerUrl");
        });

        expect(mockGetOpenIDConfigurationV1).toHaveBeenCalledWith(
            "testOidcIssuerUrl"
        );
    });

    it("useGetOpenIDConfigurationV1 should update data appropriately when making a successful REST call", async () => {
        mockGetOpenIDConfigurationV1.mockResolvedValue(
            mockOpenIDConfigurationV1
        );
        const { result, waitFor } = renderHook(() =>
            useGetOpenIDConfigurationV1()
        );
        await act(async () => {
            const promise =
                result.current.getOpenIDConfigurationV1("testOidcIssuerUrl");

            // Wait for state update
            await waitFor(() => result.current.status === ActionStatus.Initial);

            // When REST call is in progress
            expect(result.current.openIDConfigurationV1).toBeNull();
            expect(result.current.getOpenIDConfigurationV1).toBeDefined();
            expect(result.current.status).toEqual(ActionStatus.Working);
            expect(result.current.errorMessage).toEqual("");

            return promise.then(() => {
                // When REST call is completed
                expect(result.current.openIDConfigurationV1).toEqual(
                    mockOpenIDConfigurationV1
                );
                expect(result.current.getOpenIDConfigurationV1).toBeDefined();
                expect(result.current.status).toEqual(ActionStatus.Done);
                expect(result.current.errorMessage).toEqual("");
            });
        });
    });

    it("useGetOpenIDConfigurationV1 should update data appropriately when making an unsuccessful REST call", async () => {
        mockGetOpenIDConfigurationV1.mockRejectedValue(mockError);
        const { result, waitFor } = renderHook(() =>
            useGetOpenIDConfigurationV1()
        );
        await act(async () => {
            const promise =
                result.current.getOpenIDConfigurationV1("testOidcIssuerUrl");

            // Wait for state update
            await waitFor(() => result.current.status === ActionStatus.Initial);

            // When REST call is in progress
            expect(result.current.openIDConfigurationV1).toBeNull();
            expect(result.current.getOpenIDConfigurationV1).toBeDefined();
            expect(result.current.status).toEqual(ActionStatus.Working);
            expect(result.current.errorMessage).toEqual("");

            return promise.then(() => {
                // When REST call is completed
                expect(result.current.openIDConfigurationV1).toBeNull();
                expect(result.current.getOpenIDConfigurationV1).toBeDefined();
                expect(result.current.status).toEqual(ActionStatus.Error);
                expect(result.current.errorMessage).toEqual("testError");
            });
        });
    });
});

let mockGetOpenIDConfigurationV1: jest.Mock;

const mockOpenIDConfigurationV1 = {
    issuer: "testIssuer",
};

const mockError = {
    response: {
        data: {
            message: "testError",
        },
    },
};
