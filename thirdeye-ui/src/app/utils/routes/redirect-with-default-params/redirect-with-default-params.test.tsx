/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
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
    useSearchParams: jest.fn().mockImplementation(() => {
        return [mockUrlSearchParamsInstance];
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

    it("should have called navigate with the time range values from customDurationGenerator in query string", async () => {
        const customDurationGenerator = (): [number, number] => {
            return [500, 600];
        };
        render(
            <RedirectWithDefaultParams
                customDurationGenerator={customDurationGenerator}
                replace={false}
                to="path-to-redirect-to"
            >
                Hello world
            </RedirectWithDefaultParams>
        );

        expect(await screen.findByText("Hello world")).toBeInTheDocument();
        expect(mockNavigate).toHaveBeenLastCalledWith(
            "path-to-redirect-to?timeRange=CUSTOM&startTime=500&endTime=600",
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

const mockUrlSearchParamsInstance = new URLSearchParams();
const mockNavigate = jest.fn();
const mockLocation = jest.fn();
const mockGetLastUsedForPath = jest.fn();
