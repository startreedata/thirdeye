import { fireEvent, render, screen } from "@testing-library/react";
import React, { ReactNode } from "react";
import { VisualizationCard } from "./visualization-card.component";
import { VisualizationCardProps } from "./visualization-card.interfaces";

describe("VisualizationCard", () => {
    describe("when the component is maximized", () => {
        it("happy path", async () => {
            render(<VisualizationCard {...mockDefaultProps} />);

            expect(mockFunction).toHaveBeenCalled();
            expect(
                await screen.findByText("TestHelperText")
            ).toBeInTheDocument();
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

        it("should not show the helper text if it's empty", async () => {
            const props = {
                ...mockDefaultProps,
                helperText: "",
            } as VisualizationCardProps;
            render(<VisualizationCard {...props} />);

            expect(mockFunction).toHaveBeenCalled();
            expect(
                screen.queryByText("TestHelperText")
            ).not.toBeInTheDocument();
        });

        it("should be able to click the refresh button if it's visible", async () => {
            render(<VisualizationCard {...mockDefaultProps} />);

            expect(mockFunction).toHaveBeenCalled();

            fireEvent.click(screen.getByTestId("refresh-button-maximized"));

            expect(mockFunction).toHaveBeenCalledTimes(2);
        });

        it("should not show the refresh button if it's variable is true", async () => {
            const props = {
                ...mockDefaultProps,
                hideRefreshButton: true,
            } as VisualizationCardProps;
            render(<VisualizationCard {...props} />);

            expect(mockFunction).toHaveBeenCalled();
            expect(
                screen.queryByTestId("refresh-button-maximized")
            ).not.toBeInTheDocument();
        });
    });

    describe("when the component is not maximized", () => {
        it("happy path", async () => {
            const props = {
                ...mockDefaultProps,
                maximized: false,
            } as VisualizationCardProps;

            render(<VisualizationCard {...props} />);

            expect(mockFunction).toHaveBeenCalled();
            expect(
                screen.queryByText("TestHelperText")
            ).not.toBeInTheDocument();
            expect(
                screen.queryByTestId("refresh-button-maximized")
            ).not.toBeInTheDocument();
            expect(
                screen.queryByTestId("restore-button-maximized")
            ).not.toBeInTheDocument();
            expect(screen.queryByText("TestTitle")).not.toBeInTheDocument();
            expect(await screen.findByText("TestChildren")).toBeInTheDocument();
            expect(
                screen.queryByTestId("maximized-visualization-card-placeholder")
            ).not.toBeInTheDocument();
        });
    });

    it("when the children is not passed in the component", async () => {
        const props = {
            ...mockDefaultProps,
            maximized: false,
            children: null,
        } as VisualizationCardProps;

        render(<VisualizationCard {...props} />);

        expect(mockFunction).toHaveBeenCalled();
        expect(screen.queryByText("TestHelperText")).not.toBeInTheDocument();
        expect(
            screen.queryByTestId("refresh-button-maximized")
        ).not.toBeInTheDocument();
        expect(
            screen.queryByTestId("restore-button-maximized")
        ).not.toBeInTheDocument();
        expect(screen.queryByText("TestTitle")).not.toBeInTheDocument();
        expect(screen.queryByText("TestChildren")).not.toBeInTheDocument();
        expect(
            screen.queryByTestId("maximized-visualization-card-placeholder")
        ).not.toBeInTheDocument();
    });
});

const mockFunction = jest.fn();

const mockDefaultProps = {
    maximized: true,
    visualizationHeight: 10,
    visualizationMaximizedHeight: 10,
    title: "TestTitle",
    error: false,
    helperText: "TestHelperText",
    hideRefreshButton: false,
    onRefresh: mockFunction,
    onMaximize: mockFunction,
    onRestore: mockFunction,
    children: (<p>TestChildren</p>) as ReactNode,
} as VisualizationCardProps;
