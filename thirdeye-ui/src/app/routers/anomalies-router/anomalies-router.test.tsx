import { render, screen } from "@testing-library/react";
import React from "react";
import { MemoryRouter } from "react-router-dom";
import { Breadcrumb } from "../../components/breadcrumbs/breadcrumbs.interfaces";
import { AppRoute } from "../../utils/routes-util/routes-util";
import { AnomaliesRouter } from "./anomalies-router";

jest.mock("../../components/app-breadcrumbs/app-breadcrumbs.component", () => ({
    useAppBreadcrumbs: jest.fn().mockImplementation(() => ({
        setRouterBreadcrumbs: mockSetRouterBreadcrumbs,
    })),
}));

jest.mock("../../store/app-toolbar-store/app-toolbar-store", () => ({
    useAppToolbarStore: jest.fn().mockImplementation((selector) => {
        return selector({
            removeAppToolbar: mockRemoveAppToolbar,
        });
    }),
}));

jest.mock("react-router-dom", () => ({
    ...(jest.requireActual("react-router-dom") as Record<string, unknown>),
    useHistory: jest.fn().mockImplementation(() => ({
        push: mockPush,
    })),
}));

jest.mock("react-i18next", () => ({
    useTranslation: jest.fn().mockReturnValue({
        t: (key: string): string => {
            return key;
        },
    }),
}));

jest.mock("../../utils/routes-util/routes-util", () => ({
    ...(jest.requireActual("../../utils/routes-util/routes-util") as Record<
        string,
        unknown
    >),
    getAnomaliesPath: jest.fn().mockReturnValue("testAnomaliesPath"),
}));

jest.mock(
    "../../pages/anomalies-all-page/anomalies-all-page.component",
    () => ({
        AnomaliesAllPage: jest
            .fn()
            .mockImplementation(() => <>testAnomaliesAllPage</>),
    })
);

jest.mock(
    "../../pages/anomalies-detail-page/anomalies-detail-page.component",
    () => ({
        AnomaliesDetailPage: jest
            .fn()
            .mockImplementation(() => <>testAnomaliesDetailPage</>),
    })
);

jest.mock(
    "../../pages/page-not-found-page/page-not-found-page.component",
    () => ({
        PageNotFoundPage: jest
            .fn()
            .mockImplementation(() => <>testPageNotFoundPage</>),
    })
);

describe("Anomalies Router", () => {
    test("should set appropriate router breadcrumbs", () => {
        render(
            <MemoryRouter>
                <AnomaliesRouter />
            </MemoryRouter>
        );

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

    test("should remove app toolbar", () => {
        render(
            <MemoryRouter>
                <AnomaliesRouter />
            </MemoryRouter>
        );

        expect(mockRemoveAppToolbar).toHaveBeenCalled();
    });

    test("should render anomalies all page at exact anomalies path", () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.ANOMALIES]}>
                <AnomaliesRouter />
            </MemoryRouter>
        );

        expect(screen.getByText("testAnomaliesAllPage")).toBeInTheDocument();
    });

    test("should render page not found page at invalid anomalies path", () => {
        render(
            <MemoryRouter initialEntries={[`${AppRoute.ANOMALIES}/testPath`]}>
                <AnomaliesRouter />
            </MemoryRouter>
        );

        expect(screen.getByText("testPageNotFoundPage")).toBeInTheDocument();
    });

    test("should render anomalies all page at exact anomalies all path", () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.ANOMALIES_ALL]}>
                <AnomaliesRouter />
            </MemoryRouter>
        );

        expect(screen.getByText("testAnomaliesAllPage")).toBeInTheDocument();
    });

    test("should render page not found page at invalid anomalies all path", () => {
        render(
            <MemoryRouter
                initialEntries={[`${AppRoute.ANOMALIES_ALL}/testPath`]}
            >
                <AnomaliesRouter />
            </MemoryRouter>
        );

        expect(screen.getByText("testPageNotFoundPage")).toBeInTheDocument();
    });

    test("should render anomalies detail page at exact anomalies detail path", () => {
        render(
            <MemoryRouter initialEntries={[AppRoute.ANOMALIES_DETAIL]}>
                <AnomaliesRouter />
            </MemoryRouter>
        );

        expect(screen.getByText("testAnomaliesDetailPage")).toBeInTheDocument();
    });

    test("should render page not found page at invalid anomalies detail path", () => {
        render(
            <MemoryRouter
                initialEntries={[`${AppRoute.ANOMALIES_DETAIL}/testPath`]}
            >
                <AnomaliesRouter />
            </MemoryRouter>
        );

        expect(screen.getByText("testPageNotFoundPage")).toBeInTheDocument();
    });

    test("should render page not found page at any other path", () => {
        render(
            <MemoryRouter initialEntries={["/testPath"]}>
                <AnomaliesRouter />
            </MemoryRouter>
        );

        expect(screen.getByText("testPageNotFoundPage")).toBeInTheDocument();
    });

    test("should render page not found page by default", () => {
        render(
            <MemoryRouter>
                <AnomaliesRouter />
            </MemoryRouter>
        );

        expect(screen.getByText("testPageNotFoundPage")).toBeInTheDocument();
    });
});

const mockSetRouterBreadcrumbs = jest.fn();

const mockRemoveAppToolbar = jest.fn();

const mockPush = jest.fn();
