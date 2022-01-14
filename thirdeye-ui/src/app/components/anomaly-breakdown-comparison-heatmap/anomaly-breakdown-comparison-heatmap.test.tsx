import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { rest } from "msw";
import { setupServer } from "msw/node";
import React from "react";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import { AnomalyBreakdownComparisonHeatmap } from "./anomaly-breakdown-comparison-heatmap.component";

const server = setupServer(
    rest.get("/api/rca/metrics/breakdown/anomaly/451751", (req, res, ctx) => {
        if (req.url.searchParams.get("offset") === "wo1w") {
            return res(ctx.json(mockCompareDataResponse));
        }

        return res(ctx.json(mockCurrentResponseData));
    })
);

beforeAll(() => server.listen());

afterEach(() => server.resetHandlers());

afterAll(() => server.close());

it("AnomalyBreakdownComparisonHeatmap should show all the UI components for valid data responses", async () => {
    expect.assertions(8);

    render(
        <AnomalyBreakdownComparisonHeatmap
            anomaly={mockAnomalyDetailsResponse as Anomaly}
            anomalyId={451751}
        />
    );

    // Wait for the filter controls to be rendered
    const filterDataControlsContainer = await screen.findByText(
        "Filter Data Controls"
    );

    expect(filterDataControlsContainer).toBeInTheDocument();

    const tooltipReferenceContainer = await screen.findByText(
        "Tooltip Reference"
    );

    expect(tooltipReferenceContainer).toBeInTheDocument();

    // Ensure a treemap shows for each dimension in the data payload.
    // These are the labels for each treemap
    const containersForTreemaps = [
        "browser",
        "country",
        "device",
        "gender",
        "os",
        "version",
    ].map(async (dimensionColumn) => {
        const containerForTreemapForDimension = await screen.findByText(
            dimensionColumn
        );

        expect(containerForTreemapForDimension).toBeInTheDocument();
    });

    await Promise.all(containersForTreemaps);
});

it("AnomalyBreakdownComparisonHeatmap should show error text if error in data request", async () => {
    server.resetHandlers(
        rest.get(
            "/api/rca/metrics/breakdown/anomaly/451751",
            (req, res, ctx) => {
                if (req.url.searchParams.get("offset") === "wo1w") {
                    return res(ctx.status(500));
                }

                return res(ctx.json(mockCurrentResponseData));
            }
        )
    );

    expect.assertions(1);

    render(
        <AnomalyBreakdownComparisonHeatmap
            anomaly={mockAnomalyDetailsResponse as Anomaly}
            anomalyId={451751}
        />
    );

    // Wait for the error message to show (when the data requests resolves)
    const errorMessageContainer = await screen.findByText(
        /An issue was experienced while retrieving data/
    );

    expect(errorMessageContainer).toBeInTheDocument();
});

it("AnomalyBreakdownComparisonHeatmap should show filter pill when tile is clicked and removed when clicked on", async () => {
    expect.assertions(2);

    render(
        <AnomalyBreakdownComparisonHeatmap
            anomaly={mockAnomalyDetailsResponse as Anomaly}
            anomalyId={451751}
            shouldTruncateText={false}
        />
    );

    const chromeTile = await screen.findByText("chrome");
    userEvent.click(chromeTile);

    const chromePill = await screen.findByText(/browser=chrome/);

    // Check chromePill so typescript does not complain
    if (!chromePill || !chromePill.parentElement) {
        return;
    }

    expect(chromePill).toBeInTheDocument();

    userEvent.click(chromePill.parentElement.children[1]);

    expect(chromePill).not.toBeInTheDocument();
});

const mockCurrentResponseData = {
    browser: {
        chrome: 547246.0,
        safari: 218694.0,
        firefox: 54466.0,
        ie: 21606.0,
        others: 251496.0,
    },
    country: { BR: 218620.0, IN: 382886.0, US: 437640.0, CA: 54362.0 },
    device: { tablet: 54206.0, desktop: 382718.0, phone: 656584.0 },
    gender: { F: 546754.0, M: 546754.0 },
    os: {
        Linux: 54444.0,
        OSX: 109184.0,
        Windows: 218700.0,
        iOS: 328214.0,
        Android: 382966.0,
    },
    version: { "0.1": 21366.0, "0.2": 87034.0, "0.3": 985108.0 },
};

const mockCompareDataResponse = {
    browserColumn: {
        chrome: 360666.0,
        safari: 216624.0,
        firefox: 53886.0,
        ie: 21340.0,
        others: 249150.0,
    },
    country: { BR: 180232.0, IN: 315746.0, US: 360930.0, CA: 44758.0 },
    device: { tablet: 44630.0, desktop: 315594.0, phone: 541442.0 },
    gender: { F: 450833.0, M: 450833.0 },
    os: {
        Linux: 44834.0,
        OSX: 89986.0,
        Windows: 180334.0,
        iOS: 270674.0,
        Android: 315838.0,
    },
    version: { "0.1": 17514.0, "0.2": 71684.0, "0.3": 812468.0 },
};

const mockAnomalyDetailsResponse = {
    id: 451751,
    startTime: 1585353600000,
    endTime: 1585526400000,
    avgCurrentVal: 360957.0,
    avgBaselineVal: 196314.0,
    score: 0.0,
    weight: 0.0,
    impactToGlobal: 0.0,
    sourceType: "ANOMALY_REPLAY",
    created: 1640811064046,
    notified: true,
    alert: { id: 451747, name: "pageviews-percentage-change" },
};
