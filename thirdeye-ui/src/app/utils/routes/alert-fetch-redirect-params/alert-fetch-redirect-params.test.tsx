import { render, screen } from "@testing-library/react";
import axios from "axios";
import React from "react";
import { AlertFetchRedirectParams } from "./alert-fetch-redirect-params.component";

jest.mock("react-router-dom", () => ({
    useNavigate: jest.fn().mockImplementation(() => {
        return mockNavigate;
    }),
    useLocation: jest.fn().mockImplementation(() => {
        return mockLocation;
    }),
    useSearchParams: jest.fn().mockImplementation(() => {
        return [mockUrlSearchParamsInstance];
    }),
    useParams: jest.fn().mockImplementation(() => {
        return { id: "123" };
    }),
    resolvePath: jest.fn().mockImplementation((arg) => {
        return arg;
    }),
}));

jest.mock(
    "../../../stores/last-used-params/last-used-search-params.store",
    () => ({
        useLastUsedSearchParams: jest.fn().mockImplementation(() => {
            return {
                getLastUsedForPath: mockGetLastUsedForPath,
            };
        }),
    })
);

describe("Alert Fetch Redirect Params", () => {
    it("should have called navigate with the time range values in query string returned from API request", async () => {
        jest.spyOn(axios, "post").mockResolvedValueOnce({
            data: {
                defaultStartTime: 10,
                defaultEndTime: 11,
            },
        });
        render(
            <AlertFetchRedirectParams
                fallbackDurationGenerator={mockFallbackFunction}
                replace={false}
                to="path-to-redirect-to"
            >
                Hello world
            </AlertFetchRedirectParams>
        );

        expect(await screen.findByText("Hello world")).toBeInTheDocument();
        expect(mockNavigate).toHaveBeenLastCalledWith(
            "path-to-redirect-to?timeRange=CUSTOM&startTime=10&endTime=11",
            { replace: false }
        );
    });

    it("should have called navigate with the time range values from fallbackDurationGenerator in query string is API request fails", async () => {
        jest.spyOn(axios, "post").mockRejectedValueOnce({
            data: {},
        });
        render(
            <AlertFetchRedirectParams
                fallbackDurationGenerator={mockFallbackFunction}
                replace={false}
                to="path-to-redirect-to"
            >
                Hello world
            </AlertFetchRedirectParams>
        );

        expect(await screen.findByText("Hello world")).toBeInTheDocument();
        expect(mockNavigate).toHaveBeenLastCalledWith(
            "path-to-redirect-to?timeRange=CUSTOM&startTime=1&endTime=2",
            { replace: false }
        );
    });

    it("should have called navigate with the time range values in query string if there are no last used paths for key", async () => {
        jest.spyOn(axios, "get").mockRejectedValueOnce({
            data: {},
        });
        mockGetLastUsedForPath.mockReturnValue(undefined);
        render(
            <AlertFetchRedirectParams
                fallbackDurationGenerator={mockFallbackFunction}
                replace={false}
                to="path-to-redirect-to"
            >
                Hello world
            </AlertFetchRedirectParams>
        );

        expect(await screen.findByText("Hello world")).toBeInTheDocument();
        expect(mockNavigate).toHaveBeenLastCalledWith(
            "path-to-redirect-to?timeRange=CUSTOM&startTime=1&endTime=2",
            { replace: false }
        );
    });
});

const mockFallbackFunction = (): [number, number] => [1, 2];
const mockUrlSearchParamsInstance = new URLSearchParams();
const mockNavigate = jest.fn();
const mockLocation = jest.fn();
const mockGetLastUsedForPath = jest.fn();
