import React, { FunctionComponent, lazy, Suspense } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import {
    AppRoute,
    AppRouteRelative,
    getBasePath,
    getHomePath,
} from "../../utils/routes/routes.util";

const HomePage = lazy(() =>
    import(
        /* webpackChunkName: "home-page" */ "../../pages/home-page/home-page.component"
    ).then((module) => ({ default: module.HomePage }))
);

const AdminPage = lazy(() =>
    import(
        /* webpackChunkName: "admin-page" */ "../../pages/admin-page/admin-page.component"
    ).then((module) => ({ default: module.AdminPage }))
);

const CohortDetectorPage = lazy(() =>
    import(
        /* webpackChunkName: "cohort-detector-page" */ "../../pages/cohort-detector-page/cohort-detector-page.component"
    ).then((module) => ({ default: module.CohortDetectorPage }))
);

const LogoutPage = lazy(() =>
    import(
        /* webpackChunkName: "logout-page" */ "../../pages/logout-page/logout-page.component"
    ).then((module) => ({ default: module.LogoutPage }))
);

const PageNotFoundPage = lazy(() =>
    import(
        /* webpackChunkName: "page-not-found-page" */ "../../pages/page-not-found-page/page-not-found-page.component"
    ).then((module) => ({ default: module.PageNotFoundPage }))
);

export const GeneralAuthenticatedRouter: FunctionComponent = () => {
    return (
        <Suspense fallback={<AppLoadingIndicatorV1 />}>
            <Routes>
                {/* Base path */}
                {/* Redirect to home path */}
                <Route
                    element={<Navigate replace to={getHomePath()} />}
                    path={AppRoute.BASE}
                />

                {/* Home path */}
                <Route
                    element={<HomePage />}
                    path={`${AppRouteRelative.HOME}`}
                />

                {/* Admin path */}
                <Route
                    element={<AdminPage />}
                    path={`${AppRouteRelative.ADMIN}`}
                />

                {/* Cohort detector path */}
                <Route
                    element={<CohortDetectorPage />}
                    path={`${AppRouteRelative.COHORT_DETECTOR}`}
                />

                {/* Login path */}
                {/* Already authenticated, redirect to base path */}
                <Route
                    element={<Navigate replace to={getBasePath()} />}
                    path={AppRouteRelative.LOGIN}
                />

                {/* Logout path */}
                <Route
                    element={<LogoutPage />}
                    path={AppRouteRelative.LOGOUT}
                />

                {/* No match found, render page not found */}
                <Route element={<PageNotFoundPage />} path="*" />
            </Routes>
        </Suspense>
    );
};
