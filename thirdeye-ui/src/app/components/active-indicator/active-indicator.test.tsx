import { render, screen } from "@testing-library/react";
import React from "react";
import { Color } from "../../utils/material-ui/color.util";
import { ActiveIndicator } from "./active-indicator.component";
import { ActiveIndicatorProps } from "./active-indicator.interfaces";

describe("ActiveIndicator", () => {
    it("should render an indicator for the active state", async () => {
        render(<ActiveIndicator {...mockActiveIndicatorProps} />);

        expect(
            await screen.findByTestId("activity-indicator-icon")
        ).toHaveAttribute("color", expectedActiveColor);
    });

    it("should render an indicator for the inactive state", async () => {
        const props = { ...mockActiveIndicatorProps, active: false };
        render(<ActiveIndicator {...props} />);

        expect(
            await screen.findByTestId("activity-indicator-icon")
        ).toHaveAttribute("color", expectedInactiveColor);
    });
});

const mockActiveIndicatorProps = {
    active: true,
} as ActiveIndicatorProps;

const expectedActiveColor: string = Color.GREEN_5.toLowerCase();
const expectedInactiveColor: string = Color.RED_2.toLowerCase();
