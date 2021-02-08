import { render, screen } from "@testing-library/react";
import React from "react";
import { MemoryRouter } from "react-router-dom";
import { Breadcrumb } from "../../components/breadcrumbs/breadcrumbs.interfaces";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { AppRoute } from "../../utils/routes/routes.util";
import { AnomaliesRouter } from "./anomalies.router";

jest.mock("../../components/app-breadcrumbs/app-breadcrumbs.component", () => ({
    useAppBreadcrumbs: jest.fn().mockImplementation(() => ({
        setRouterBreadcrumbs: mockSetRouterBreadcrumbs,
    })),
}));

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
    "../../components/loading-indicator/loading-indicator.component",
    () => ({
        LoadingIndicator: jest.fn().mockReturnValue(<>testLoadingIndicator</>),
    })
);

jest.mock(
    "../../pages/anomalies-all-page/anomalies-all-page.component",
    () => ({
        AnomaliesAllPage: jest.fn().mockReturnValue(<>testAnomaliesAllPage</>),
    })
);

jest.mock(
    "../../pages/anomalies-detail-page/anomalies-detail-page.component",
    () => ({
        AnomaliesDetailPage: jest
            .fn()
            .mockReturnValue(<>testAnomaliesDetailPage</>),
    })
);

jest.mock(
    "../../pages/page-not-found-page/page-not-found-page.component",
    () => ({
        PageNotFoundPage: jest.fn().mockReturnValue(<>testPageNotFoundPage</>),
    })
);

describe("Anomalies Router", () => {
    test("should have rendered loading indicator while loading", () => {
        render(
            <MemoryRouter>
                <AnomaliesRouter />
            </MemoryRouter>
        );

        expect(LoadingIndicator).toHaveBeenCalled();
    });

    test("should set appropriate router breadcrumbs", () => {
        render(
            <MemoryRouter>
                <AnomaliesRouter />
            </MemoryRouter>
        );

        expect(mockSetRouterBreadcrumbs).toHaveBeenCalled();

        // Get router breadcrumbs
        const breadcrumbs: Breadcrumb[] =
            mockSetRouterBreadcrumbs.mock.calls[0][0];
        // Also invoke the click handlers
        breadcrumbs &&
            breadcrumbs[0] &&
            breadcrumbs[0].onClick &&
            breadcrumbs[0].onClick();

        expect(breadcrumbs).toHaveLength(1);
        expect(breadcrumbs[0].text).toEqual("label.anomalies");
        expect(breadcrumbs[0].onClick).toBeDefined();
        expect(mockPush).toHaveBeenCalledWith("testAnomaliesPath");
    });

    test("should render anomalies all page at exact anomalies path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.ANOMALIES]}>
                <AnomaliesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAnomaliesAllPage")
        ).resolves.toBeInTheDocument();
    });

    test("should render page not found page at invalid anomalies path", async () => {
        render(
            <MemoryRouter initialEntries={[`${AppRoute.ANOMALIES}/testPath`]}>
                <AnomaliesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    test("should render anomalies all page at exact anomalies all path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.ANOMALIES_ALL]}>
                <AnomaliesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAnomaliesAllPage")
        ).resolves.toBeInTheDocument();
    });

    test("should render page not found page at invalid anomalies all path", async () => {
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

    test("should render anomalies detail page at exact anomalies detail path", async () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.ANOMALIES_DETAIL]}>
                <AnomaliesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testAnomaliesDetailPage")
        ).resolves.toBeInTheDocument();
    });

    test("should render page not found page at invalid anomalies detail path", async () => {
        render(
            <MemoryRouter
                initialEntries={[`${AppRoute.ANOMALIES_DETAIL}/testPath`]}
            >
                <AnomaliesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    test("should render page not found page at any other path", async () => {
        render(
            <MemoryRouter initialEntries={["/testPath"]}>
                <AnomaliesRouter />
            </MemoryRouter>
        );

        await expect(
            screen.findByText("testPageNotFoundPage")
        ).resolves.toBeInTheDocument();
    });

    test("should render page not found page by default", async () => {
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

const mockSetRouterBreadcrumbs = jest.fn();

const mockPush = jest.fn();
