import { render, screen } from "@testing-library/react";
import React from "react";
import { MemoryRouter } from "react-router-dom";
import { PageContainer } from "../../components/page-container/page-container.component";
import {
    AppRoute,
    getAnomaliesPath,
} from "../../utils/routes-util/routes-util";
import { AnomaliesRouter } from "./anomalies-router";

jest.mock("../../store/app-breadcrumbs-store/app-breadcrumbs-store", () => ({
    useAppBreadcrumbsStore: jest.fn().mockImplementation((selector) => {
        return selector({
            setAppSectionBreadcrumbs: mockSetAppSectionBreadcrumbs,
        });
    }),
}));

jest.mock("../../store/app-toolbar-store/app-toolbar-store", () => ({
    useAppToolbarStore: jest.fn().mockImplementation((selector) => {
        return selector({
            removeAppToolbar: mockRemoveAppToolbar,
        });
    }),
}));

jest.mock("react-i18next", () => ({
    useTranslation: jest.fn().mockReturnValue({
        t: (key: string): string => {
            return key;
        },
    }),
}));

jest.mock("../../components/page-container/page-container.component", () => ({
    PageContainer: jest.fn().mockImplementation(() => <>testPageContainer</>),
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
    beforeEach(() => {
        jest.clearAllMocks();
    });

    afterAll(() => {
        jest.resetAllMocks();
    });

    test("should have rendered page container while loading", () => {
        render(
            <MemoryRouter>
                <AnomaliesRouter />
            </MemoryRouter>
        );

        expect(PageContainer).toHaveBeenCalled();
    });

    test("should set appropriate app section breadcrumbs", () => {
        render(
            <MemoryRouter>
                <AnomaliesRouter />
            </MemoryRouter>
        );

        expect(mockSetAppSectionBreadcrumbs).toHaveBeenCalledWith([
            {
                text: "label.anomalies",
                pathFn: getAnomaliesPath,
            },
        ]);
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

    test("should render page not found page at non-exact anomalies path", () => {
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

    test("should render page not found page at non-exact anomalies all path", () => {
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

    test("should render page not found page at non-exact anomalies detail path", () => {
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

const mockSetAppSectionBreadcrumbs = jest.fn();

const mockRemoveAppToolbar = jest.fn();
