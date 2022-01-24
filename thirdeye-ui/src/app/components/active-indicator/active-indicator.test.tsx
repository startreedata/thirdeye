import { act, render, screen } from "@testing-library/react";
import React from "react";
import { Color } from "../../utils/material-ui/color.util";
import { ActiveIndicator } from "./active-indicator.component";
import { ActiveIndicatorProps } from "./active-indicator.interfaces";

describe("ActiveIndicator", () => {
    it("should render an indicator for the active state", async () => {
        act(() => {
            render(<ActiveIndicator {...mockActiveIndicatorProps} />);
        });

        expect(
            await screen.findByTestId("activity-indicator-icon")
        ).toHaveAttribute("color", mockActiveColor);
    });

    it("should render an indicator for the inactive state", async () => {
        const props = { ...mockActiveIndicatorProps, active: false };
        act(() => {
            render(<ActiveIndicator {...props} />);
        });

        expect(
            await screen.findByTestId("activity-indicator-icon")
        ).toHaveAttribute("color", mockInactiveColor);
    });
});

const mockActiveIndicatorProps = {
    active: true,
} as ActiveIndicatorProps;

const mockActiveColor: string = Color.GREEN_5.toLowerCase();
const mockInactiveColor: string = Color.RED_2.toLowerCase();
