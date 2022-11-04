import { ThemeProvider } from "@material-ui/core";
import { render, screen } from "@testing-library/react";
import React from "react";
import { lightV1 } from "../../platform/utils";
import { ColorV1 } from "../../platform/utils/material-ui/color.util";
import { ActiveIndicator } from "./active-indicator.component";
import { ActiveIndicatorProps } from "./active-indicator.interfaces";

describe("ActiveIndicator", () => {
    it("should render an indicator for the active state", async () => {
        render(
            <ThemeProvider theme={lightV1}>
                <ActiveIndicator {...mockActiveIndicatorProps} />
            </ThemeProvider>
        );

        expect(
            await screen.findByTestId("activity-indicator-icon")
        ).toHaveAttribute("color", expectedActiveColor);
    });

    it("should render an indicator for the inactive state", async () => {
        const props = { ...mockActiveIndicatorProps, active: false };
        render(
            <ThemeProvider theme={lightV1}>
                <ActiveIndicator {...props} />
            </ThemeProvider>
        );

        expect(
            await screen.findByTestId("activity-indicator-icon")
        ).toHaveAttribute("color", expectedInactiveColor);
    });
});

const mockActiveIndicatorProps = {
    active: true,
} as ActiveIndicatorProps;

const expectedActiveColor: string = ColorV1.Green2;
const expectedInactiveColor: string = ColorV1.Red2;
