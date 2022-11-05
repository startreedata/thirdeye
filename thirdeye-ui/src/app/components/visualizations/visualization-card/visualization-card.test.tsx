/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { fireEvent, render, screen } from "@testing-library/react";
import React, { ReactNode } from "react";
import { VisualizationCard } from "./visualization-card.component";
import { VisualizationCardProps } from "./visualization-card.interfaces";

describe("VisualizationCard", () => {
    describe("when the component is maximized", () => {
        it("should render expected elements", async () => {
            render(<VisualizationCard {...mockDefaultProps} />);

            expect(mockOnMaximizeFunction).toHaveBeenCalled();
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

            expect(mockOnMaximizeFunction).toHaveBeenCalled();
            expect(
                screen.queryByText("TestHelperText")
            ).not.toBeInTheDocument();
        });

        it("should be able to click the refresh button if it's visible", async () => {
            render(<VisualizationCard {...mockDefaultProps} />);

            expect(mockOnMaximizeFunction).toHaveBeenCalled();

            fireEvent.click(screen.getByTestId("refresh-button-maximized"));

            expect(mockOnRefreshFunction).toHaveBeenCalledTimes(1);
        });

        it("should be able to click the restore button", async () => {
            render(<VisualizationCard {...mockDefaultProps} />);

            expect(mockOnMaximizeFunction).toHaveBeenCalled();

            fireEvent.click(screen.getByTestId("restore-button-maximized"));

            expect(mockOnRestoreFunction).toHaveBeenCalledTimes(1);
        });

        it("should not show the refresh button if it's variable is true", async () => {
            const props = {
                ...mockDefaultProps,
                hideRefreshButton: true,
            } as VisualizationCardProps;
            render(<VisualizationCard {...props} />);

            expect(mockOnMaximizeFunction).toHaveBeenCalled();
            expect(
                screen.queryByTestId("refresh-button-maximized")
            ).not.toBeInTheDocument();
        });
    });

    describe("when the component is not maximized", () => {
        it("should render expected elements", async () => {
            const props = {
                ...mockDefaultProps,
                maximized: false,
            } as VisualizationCardProps;

            render(<VisualizationCard {...props} />);

            expect(mockOnRestoreFunction).toHaveBeenCalled();
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

    it("should render fine if the children is not passed in the component", async () => {
        const props = {
            ...mockDefaultProps,
            maximized: false,
            children: null,
        } as VisualizationCardProps;

        render(<VisualizationCard {...props} />);

        expect(mockOnRestoreFunction).toHaveBeenCalled();
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

    it("should call onRestore when the escape button is pressed", async () => {
        const props = {
            ...mockDefaultProps,
            maximized: false,
        } as VisualizationCardProps;

        render(<VisualizationCard {...props} />);

        fireEvent.keyDown(await screen.findByText("TestChildren"), {
            key: "Escape",
            code: "Escape",
            keyCode: 27,
            charCode: 27,
        });

        expect(mockOnRestoreFunction).toHaveBeenCalledTimes(1);
    });
});

const mockOnRefreshFunction = jest.fn();
const mockOnMaximizeFunction = jest.fn();
const mockOnRestoreFunction = jest.fn();

const mockDefaultProps = {
    maximized: true,
    visualizationHeight: 10,
    visualizationMaximizedHeight: 10,
    title: "TestTitle",
    error: false,
    helperText: "TestHelperText",
    hideRefreshButton: false,
    onRefresh: mockOnRefreshFunction,
    onMaximize: mockOnMaximizeFunction,
    onRestore: mockOnRestoreFunction,
    children: (<p>TestChildren</p>) as ReactNode,
} as VisualizationCardProps;
