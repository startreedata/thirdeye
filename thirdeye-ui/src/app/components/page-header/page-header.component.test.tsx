import { render, screen } from "@testing-library/react";
import React from "react";
import { Router } from "react-router-dom";
import { historyV1 } from "../../platform/utils";
import { TimeRangeProvider } from "../time-range/time-range-provider/time-range-provider.component";
import { PageHeader } from "./page-header.component";

jest.mock("../../platform/components", () => ({
    ...(jest.requireActual("../../platform/components") as Record<
        string,
        unknown
    >),
    PageV1: jest.fn().mockImplementation((props) => props.children),
    PageHeaderV1: jest.fn().mockImplementation((props) => props.children),
    PageHeaderTextV1: jest.fn().mockImplementation((props) => props.children),
    PageHeaderActionsV1: jest
        .fn()
        .mockImplementation((props) => props.children),
}));

// Force the time range selector to render fully
jest.mock("@material-ui/core", () => ({
    ...(jest.requireActual("@material-ui/core") as Record<string, unknown>),
    useMediaQuery: jest.fn().mockImplementation(() => true),
}));

jest.mock("i18next", () => ({
    ...(jest.requireActual("i18next") as Record<string, unknown>),
    t: jest.fn().mockImplementation((t) => t),
}));

describe("Page Header", () => {
    it("should render passed title and time range selector", async () => {
        render(
            <Router history={historyV1}>
                <TimeRangeProvider>
                    <PageHeader showTimeRange title="Hello world" />
                </TimeRangeProvider>
            </Router>
        );

        const titleContainer = await screen.getByText("Hello world");

        expect(titleContainer).toBeInTheDocument();

        const timeSelectorContainer = await screen.getByText("label.today");

        expect(timeSelectorContainer).toBeInTheDocument();
    });

    it("should render without time range selector", async () => {
        render(
            <Router history={historyV1}>
                <TimeRangeProvider>
                    <PageHeader title="Hello world" />
                </TimeRangeProvider>
            </Router>
        );

        const titleContainer = await screen.getByText("Hello world");

        expect(titleContainer).toBeInTheDocument();

        const timeSelectorContainer = await screen.queryByText("label.today");

        expect(timeSelectorContainer).toBeNull();

        const createMenuButton = await screen.queryByText("label.create");

        expect(createMenuButton).toBeNull();
    });

    it("should render create menu button if showCreateButton is true", () => {
        render(
            <Router history={historyV1}>
                <TimeRangeProvider>
                    <PageHeader showCreateButton title="Hello world" />
                </TimeRangeProvider>
            </Router>
        );

        const createMenuButton = screen.getByText("label.create");

        expect(createMenuButton).toBeInTheDocument();
    });
});
