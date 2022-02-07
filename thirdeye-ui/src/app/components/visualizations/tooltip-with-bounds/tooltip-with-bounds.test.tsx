import { render, screen } from "@testing-library/react";
import React, { ReactNode } from "react";
import { TooltipWithBounds } from "./tooltip-with-bounds.component";
import { TooltipWithBoundsProps } from "./tooltip-with-bounds.interfaces";

describe("TooltipWithBounds", () => {
    it("component should load title and children correctly", async () => {
        render(
            <TooltipWithBounds {...mockDefaultProps}>
                <p>TestChildren</p>
            </TooltipWithBounds>
        );

        expect(await screen.findByText("TestTitle")).toBeInTheDocument();
        expect(await screen.findByText("TestChildren")).toBeInTheDocument();
    });

    it("component should not load title if tooltip is not open", async () => {
        const props = { ...mockDefaultProps, open: false };
        render(
            <TooltipWithBounds {...props}>
                <p>TestChildren</p>
            </TooltipWithBounds>
        );

        expect(screen.queryByText("TestTitle")).not.toBeInTheDocument();
    });
});

const mockDefaultProps = {
    top: 10,
    left: 10,
    open: true,
    title: "TestTitle" as ReactNode,
} as TooltipWithBoundsProps;
