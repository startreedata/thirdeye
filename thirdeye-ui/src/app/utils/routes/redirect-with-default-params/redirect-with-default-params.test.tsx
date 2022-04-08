import { render, screen } from "@testing-library/react";
import React from "react";
import { RedirectWithDefaultParams } from "./redirect-with-default-params.component";

jest.mock("react-router-dom", () => ({
    useNavigate: jest.fn().mockImplementation(() => {
        return mockNavigate;
    }),
    useLocation: jest.fn().mockImplementation(() => {
        return mockLocation;
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

jest.mock(
    "../../../components/time-range/time-range-provider/time-range-provider.component",
    () => ({
        useTimeRange: jest.fn().mockImplementation(() => {
            return {
                timeRangeDuration: mockTimeRangeDuration,
            };
        }),
    })
);

describe("Redirect With Default Params", () => {
    it("should have called navigate with the time range values in query string", async () => {
        render(
            <RedirectWithDefaultParams replace={false} to="path-to-redirect-to">
                Hello world
            </RedirectWithDefaultParams>
        );

        expect(await screen.findByText("Hello world")).toBeInTheDocument();
        expect(mockNavigate).toHaveBeenLastCalledWith(
            "path-to-redirect-to?timeRange=CUSTOM&startTime=1&endTime=2",
            { replace: false }
        );
    });

    it("should have called navigate with the time range values in query string if there are no last used paths for key", async () => {
        mockGetLastUsedForPath.mockReturnValue(undefined);
        render(
            <RedirectWithDefaultParams
                useStoredLastUsedParamsPathKey
                pathKeyOverride="hello-world-override"
                replace={false}
                to="path-to-redirect-to"
            >
                Hello world
            </RedirectWithDefaultParams>
        );

        expect(await screen.findByText("Hello world")).toBeInTheDocument();
        expect(mockNavigate).toHaveBeenLastCalledWith(
            "path-to-redirect-to?timeRange=CUSTOM&startTime=1&endTime=2",
            { replace: false }
        );
    });

    it("should have called navigate with the stored query string if valid", async () => {
        mockGetLastUsedForPath.mockReturnValue("foo=bar");
        render(
            <RedirectWithDefaultParams
                useStoredLastUsedParamsPathKey
                pathKeyOverride="hello-world-override"
                replace={false}
                to="path-to-redirect-to"
            >
                Hello world
            </RedirectWithDefaultParams>
        );

        expect(mockGetLastUsedForPath).toHaveBeenLastCalledWith(
            "hello-world-override"
        );
        expect(await screen.findByText("Hello world")).toBeInTheDocument();
        expect(mockNavigate).toHaveBeenLastCalledWith(
            "path-to-redirect-to?foo=bar",
            { replace: false }
        );
    });
});

const mockTimeRangeDuration = {
    timeRange: "CUSTOM",
    startTime: 1,
    endTime: 2,
};

const mockNavigate = jest.fn();
const mockLocation = jest.fn();
const mockGetLastUsedForPath = jest.fn();
