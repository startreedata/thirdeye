import { act, cleanup, render, screen } from "@testing-library/react";
import React, { ReactNode } from "react";
import { VisualizationCard } from "./visualization-card.component";
import { VisualizationCardProps } from "./visualization-card.interfaces";

describe("VisualizationCard", () => {
    beforeEach(() => cleanup);

    it("component should load correctly when the visualization card is maximized", async () => {
        const props = { ...mockDefaultProps, maximized: true };
        act(() => {
            render(<VisualizationCard {...props} />);
        });

        expect(mockFunction).toHaveBeenCalled();
        expect(await screen.findByText("TestHelperText")).toBeInTheDocument();
        expect(
            screen.getByTestId("refresh-button-maximized")
        ).toBeInTheDocument();
        expect(
            screen.getByTestId("restore-button-maximized")
        ).toBeInTheDocument();
        expect(await screen.findByText("TestTitle")).toBeInTheDocument();
        expect(await screen.findByText("TestChildren")).toBeInTheDocument();
        expect(
            screen.getByTestId("maximized-visualization-card-placeholder")
        ).toBeInTheDocument();
    });
});

const mockFunction = jest.fn();

const mockDefaultProps = {
    maximized: false,
    visualizationHeight: 10,
    visualizationMaximizedHeight: 10,
    title: "TestTitle", // Displayed only when maximized
    error: false,
    helperText: "TestHelperText", // Displayed only when maximized
    hideRefreshButton: false, // Refresh button displayed only when maximized
    onRefresh: mockFunction,
    onMaximize: mockFunction,
    onRestore: mockFunction,
    children: (<p>TestChildren</p>) as ReactNode,
} as VisualizationCardProps;
