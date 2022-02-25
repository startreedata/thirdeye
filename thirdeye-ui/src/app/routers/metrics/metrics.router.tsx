import React, { FunctionComponent, lazy, Suspense } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import { AppRouteRelative } from "../../utils/routes/routes.util";

const MetricsAllPage = lazy(() =>
    import(
        /* webpackChunkName: "metrics-all-page" */ "../../pages/metrics-all-page/metrics-all-page.component"
    ).then((module) => ({ default: module.MetricsAllPage }))
);

const MetricsViewPage = lazy(() =>
    import(
        /* webpackChunkName: "metrics-view-page" */ "../../pages/metrics-view-page/metrics-view-page.component"
    ).then((module) => ({ default: module.MetricsViewPage }))
);

const PageNotFoundPage = lazy(() =>
    import(
        /* webpackChunkName: "page-not-found-page" */ "../../pages/page-not-found-page/page-not-found-page.component"
    ).then((module) => ({ default: module.PageNotFoundPage }))
);

const MetricsCreatePage = lazy(() =>
    import(
        /* webpackChunkName: "metrics-create-page" */ "../../pages/metrics-create-page/metrics-create-page.component"
    ).then((module) => ({ default: module.MetricsCreatePage }))
);

const MetricsUpdatePage = lazy(() =>
    import(
        /* webpackChunkName: "metrics-update-page" */ "../../pages/metrics-update-page/metrics-update-page.component"
    ).then((module) => ({ default: module.MetricsUpdatePage }))
);

export const MetricsRouter: FunctionComponent = () => {
    return (
        <Suspense fallback={<AppLoadingIndicatorV1 />}>
            <Routes>
                {/* Metrics path */}
                {/* Redirect to metrics all path */}
                <Route
                    index
                    element={
                        <Navigate replace to={AppRouteRelative.METRICS_ALL} />
                    }
                />

                {/* Metrics all path */}
                <Route
                    element={<MetricsAllPage />}
                    path={AppRouteRelative.METRICS_ALL}
                />

                {/* Metrics view path */}
                <Route
                    element={<MetricsViewPage />}
                    path={AppRouteRelative.METRICS_VIEW}
                />

                {/* Metrics create path */}
                <Route
                    element={<MetricsCreatePage />}
                    path={AppRouteRelative.METRICS_CREATE}
                />

                {/* Metrics update path */}
                <Route
                    element={<MetricsUpdatePage />}
                    path={AppRouteRelative.METRICS_UPDATE}
                />

                {/* No match found, render page not found */}
                <Route element={<PageNotFoundPage />} path="*" />
            </Routes>
        </Suspense>
    );
};
