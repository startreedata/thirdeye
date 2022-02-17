import { render, screen } from "@testing-library/react";
import React from "react";
import { MemoryRouter } from "react-router-dom";
import { AppLoadingIndicatorV1 } from "../../platform/components/app-loading-indicator-v1/app-loading-indicator-v1.component";
import { AppRoute } from "../../utils/routes/routes.util";
import { AnomaliesRouter } from "./anomalies.router";

jest.mock("react-router-dom", () => ({
    ...(jest.requireActual("react-router-dom") as Record<string, unknown>),
    useHistory: jest.fn().mockImplementation(() => ({
        push: mockPush,
    })),
}));

jest.mock("react-i18next", () => ({
    useTranslation: jest.fn().mockReturnValue({
        t: (key: string) => key,
    }),
}));

jest.mock("../../utils/routes/routes.util", () => ({
    ...(jest.requireActual("../../utils/routes/routes.util") as Record<
        string,
        unknown
    >),
    getAnomaliesPath: jest.fn().mockReturnValue("testAnomaliesPath"),
}));

jest.mock(
    "../../platform/components/app-loading-indicator-v1/app-loading-indicator-v1.component",
    () => ({
        AppLoadingIndicatorV1: jest
            .fn()
            .mockReturnValue("testLoadingIndicatorV1"),
    })
);

jest.mock(
    "../../pages/anomalies-all-page/anomalies-all-page.component",
    () => ({
        AnomaliesAllPage: jest.fn().mockReturnValue("testAnomaliesAllPage"),
    })
);

jest.mock(
    "../../pages/anomalies-view-page/anomalies-view-page.component",
    () => ({
        AnomaliesViewPage: jest.fn().mockReturnValue("testAnomaliesViewPage"),
    })
);

jest.mock(
    "../../pages/page-not-found-page/page-not-found-page.component",
    () => ({
        PageNotFoundPage: jest.fn().mockReturnValue("testPageNotFoundPage"),
    })
);

describe("Anomalies Router", () => {
    it("should have rendered loading indicator while loading", () => {
        render(
            <MemoryRouter>
                <AnomaliesRouter />
            </MemoryRouter>
        );

        expect(AppLoadingIndicatorV1).toHaveBeenCalled();
    });

    it("should render anomalies all page at exact anomalies path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.ANOMALIES]}>
                <AnomaliesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAnomaliesAllPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid anomalies path", async () => {
        render(
            <MemoryRouter initialEntries={[`${AppRoute.ANOMALIES}/testPath`]}>
                <AnomaliesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render anomalies all page at exact anomalies all path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.ANOMALIES_ALL]}>
                <AnomaliesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAnomaliesAllPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid anomalies all path", async () => {
        render(
            <MemoryRouter
                initialEntries={[`${AppRoute.ANOMALIES_ALL}/testPath`]}
            >
                <AnomaliesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render anomalies view page at exact anomalies view path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.ANOMALIES_VIEW]}>
                <AnomaliesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAnomaliesViewPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid anomalies view path", async () => {
        render(
            <MemoryRouter
                initialEntries={[`${AppRoute.ANOMALIES_VIEW}/testPath`]}
            >
                <AnomaliesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at any other path", async () => {
        render(
            <MemoryRouter initialEntries={["/testPath"]}>
                <AnomaliesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page by default", async () => {
        render(
            <MemoryRouter>
                <AnomaliesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });
});

const mockPush = jest.fn();
