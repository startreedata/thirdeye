import { render, screen } from "@testing-library/react";
import React from "react";
import { MemoryRouter } from "react-router-dom";
import { AppLoadingIndicatorV1 } from "../../platform/components/app-loading-indicator-v1/app-loading-indicator-v1.component";
import { AppRoute } from "../../utils/routes/routes.util";
import { MetricsRouter } from "./metrics.router";

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
    getConfigurationPath: jest.fn().mockReturnValue("testConfigurationPath"),
    getMetricsPath: jest.fn().mockReturnValue("testMetricsPath"),
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
    "../../pages/metrics-create-page/metrics-create-page.component",
    () => ({
        MetricsCreatePage: jest.fn().mockReturnValue("testMetricsCreatePage"),
    })
);

jest.mock(
    "../../pages/metrics-update-page/metrics-update-page.component",
    () => ({
        MetricsUpdatePage: jest.fn().mockReturnValue("testMetricsUpdatePage"),
    })
);

jest.mock("../../pages/metrics-all-page/metrics-all-page.component", () => ({
    MetricsAllPage: jest.fn().mockReturnValue("testMetricsAllPage"),
}));

jest.mock("../../pages/metrics-view-page/metrics-view-page.component", () => ({
    MetricsViewPage: jest.fn().mockReturnValue("testMetricsViewPage"),
}));

jest.mock(
    "../../pages/page-not-found-page/page-not-found-page.component",
    () => ({
        PageNotFoundPage: jest.fn().mockReturnValue("testPageNotFoundPage"),
    })
);

describe("Metrics Router", () => {
    it("should have rendered loading indicator while loading", () => {
        render(
            <MemoryRouter>
                <MetricsRouter />
            </MemoryRouter>
        );

        expect(AppLoadingIndicatorV1).toHaveBeenCalled();
    });

    it("should render metrics create page at exact metrics create path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.METRICS_CREATE]}>
                <MetricsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testMetricsCreatePage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid metrics create path", async () => {
        render(
            <MemoryRouter
                initialEntries={[`${AppRoute.METRICS_CREATE}/testPath`]}
            >
                <MetricsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render metrics all page at exact metrics path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.METRICS]}>
                <MetricsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testMetricsAllPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid metrics path", async () => {
        render(
            <MemoryRouter initialEntries={[`${AppRoute.METRICS}/testPath`]}>
                <MetricsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render metrics all page at exact metrics all path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.METRICS_ALL]}>
                <MetricsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testMetricsAllPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid metrics all path", async () => {
        render(
            <MemoryRouter initialEntries={[`${AppRoute.METRICS_ALL}/testPath`]}>
                <MetricsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render metrics view page at exact metrics view path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.METRICS_VIEW]}>
                <MetricsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testMetricsViewPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid metrics view path", async () => {
        render(
            <MemoryRouter
                initialEntries={[`${AppRoute.METRICS_VIEW}/testPath`]}
            >
                <MetricsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render metrics update page at exact metrics update path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.METRICS_UPDATE]}>
                <MetricsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testMetricsUpdatePage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at invalid metrics update path", async () => {
        render(
            <MemoryRouter
                initialEntries={[`${AppRoute.METRICS_UPDATE}/testPath`]}
            >
                <MetricsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page at any other path", async () => {
        render(
            <MemoryRouter initialEntries={["/testPath"]}>
                <MetricsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    it("should render page not found page by default", async () => {
        render(
            <MemoryRouter>
                <MetricsRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });
});

const mockPush = jest.fn();
