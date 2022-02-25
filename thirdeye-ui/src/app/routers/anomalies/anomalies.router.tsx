import { default as React, FunctionComponent, lazy, Suspense } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import { AppRouteRelative } from "../../utils/routes/routes.util";

const AnomaliesAllPage = lazy(() =>
    import(
        /* webpackChunkName: "anomalies-all-page" */ "../../pages/anomalies-all-page/anomalies-all-page.component"
    ).then((module) => ({ default: module.AnomaliesAllPage }))
);

const AnomaliesViewPage = lazy(() =>
    import(
        /* webpackChunkName: "anomalies-view-page" */ "../../pages/anomalies-view-page/anomalies-view-page.component"
    ).then((module) => ({ default: module.AnomaliesViewPage }))
);

const AnomaliesViewIndexPage = lazy(() =>
    import(
        /* webpackChunkName: "anomalies-view-page" */ "../../pages/anomalies-view-index-page/anomalies-view-index-page.component"
    ).then((module) => ({ default: module.AnomaliesViewIndexPage }))
);

const PageNotFoundPage = lazy(() =>
    import(
        /* webpackChunkName: "page-not-found-page" */ "../../pages/page-not-found-page/page-not-found-page.component"
    ).then((module) => ({ default: module.PageNotFoundPage }))
);

export const AnomaliesRouter: FunctionComponent = () => {
    return (
        <Suspense fallback={<AppLoadingIndicatorV1 />}>
            <Routes>
                {/* Anomalies path */}
                {/* Redirect to anomalies all path */}
                <Route
                    index
                    element={
                        <Navigate replace to={AppRouteRelative.ANOMALIES_ALL} />
                    }
                />

                {/* Anomalies all path */}
                <Route
                    element={<AnomaliesAllPage />}
                    path={AppRouteRelative.ANOMALIES_ALL}
                />

                {/* Anomalies view index path to change time range*/}
                <Route
                    element={<AnomaliesViewIndexPage />}
                    path={AppRouteRelative.ANOMALIES_VIEW_INDEX}
                />

                {/* Anomalies view path */}
                <Route
                    element={<AnomaliesViewPage />}
                    path={AppRouteRelative.ANOMALIES_VIEW}
                />

                {/* No match found, render page not found */}
                <Route element={<PageNotFoundPage />} path="*" />
            </Routes>
        </Suspense>
    );
};
