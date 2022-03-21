import { render, screen } from "@testing-library/react";
import React from "react";
import { RedirectWithDefaultParams } from "./redirect-with-default-params.component";

jest.mock("react-router-dom", () => ({
    useNavigate: jest.fn().mockImplementation(() => {
        return mockNavigate;
    }),
}));

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
});

const mockTimeRangeDuration = {
    timeRange: "CUSTOM",
    startTime: 1,
    endTime: 2,
};

const mockNavigate = jest.fn();
