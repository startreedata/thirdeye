import { act, cleanup, render, screen } from "@testing-library/react";
import React from "react";
import { MetricTile } from "./metric-tile.component";
import { MetricTileProps } from "./metric-tile.interfaces";

jest.mock("react-i18next", () => ({
    useTranslation: jest.fn().mockReturnValue({
        t: (key: string) => key,
    }),
}));

describe("MetricTile", () => {
    beforeEach(() => cleanup);

    it("component should load metric value and metric name", async () => {
        act(() => {
            render(<MetricTile {...mockDefaultProps} />);
        });

        expect(await screen.findByText("TestMetricValue")).toBeInTheDocument();
        expect(await screen.findByText("TestMetricName")).toBeInTheDocument();
        expect(screen.getByTestId("metricContainer")).not.toHaveAttribute(
            "disabled"
        );
    });

    it("component should load correct classes", async () => {
        act(() => {
            render(<MetricTile {...mockDefaultProps} />);
        });

        expect(screen.getByTestId("metricValueContainer")).toHaveClass(
            "TestMetricValueClassName"
        );
        expect(screen.getByTestId("metricNameContainer")).toHaveClass(
            "TestMetricNameClassName"
        );
    });

    it("component should not show metric name if it's not present", async () => {
        const props = { ...mockDefaultProps, metricName: "" };
        act(() => {
            render(<MetricTile {...props} />);
        });

        expect(await screen.findByText("TestMetricValue")).toBeInTheDocument();
        expect(screen.queryByText("TestMetricName")).not.toBeInTheDocument();
    });

    it("component should show no-label-data marker if metric value is not present", async () => {
        const props = { ...mockDefaultProps, metricValue: "" };
        act(() => {
            render(<MetricTile {...props} />);
        });

        expect(
            await screen.findByText("label.no-data-marker")
        ).toBeInTheDocument();
        expect(await screen.findByText("TestMetricName")).toBeInTheDocument();
    });

    it("metric container button should be disabled if the clickable is false", async () => {
        const props = { ...mockDefaultProps, clickable: false };
        act(() => {
            render(<MetricTile {...props} />);
        });

        expect(screen.getByTestId("metricContainer")).toHaveAttribute(
            "disabled",
            ""
        );
    });
});

const mockFunction = jest.fn();

const mockDefaultProps = {
    metricValue: "TestMetricValue",
    metricValueClassName: "TestMetricValueClassName",
    metricName: "TestMetricName",
    metricNameClassName: "TestMetricNameClassName",
    clickable: true,
    compact: true,
    onClick: mockFunction,
} as MetricTileProps;
