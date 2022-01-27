import { act, cleanup, render, screen } from "@testing-library/react";
import React, { ReactNode } from "react";
import { TooltipWithBounds } from "./tooltip-with-bounds.component";
import { TooltipWithBoundsProps } from "./tooltip-with-bounds.interfaces";

jest.mock("react-i18next", () => ({
    useTranslation: jest.fn().mockReturnValue({
        t: (key: string) => key,
    }),
}));

describe("TooltipWithBounds", () => {
    beforeEach(() => cleanup);

    it("component should load title and children correctly", async () => {
        act(() => {
            render(<TooltipWithBounds {...mockDefaultProps} />);
        });

        expect(await screen.findByText("TestTitle")).toBeInTheDocument();
        expect(await screen.findByText("TestChildren")).toBeInTheDocument();
    });

    it("component should not load title if tooltip is not open", async () => {
        const props = { ...mockDefaultProps, open: false };
        act(() => {
            render(<TooltipWithBounds {...props} />);
        });

        expect(screen.queryByText("TestTitle")).not.toBeInTheDocument();
    });
});

const mockDefaultProps = {
    top: 10,
    left: 10,
    open: true,
    title: "TestTitle" as ReactNode,
    children: (<p>TestChildren</p>) as ReactNode,
} as TooltipWithBoundsProps;
