import { default as React, FunctionComponent, lazy, Suspense } from "react";
import { Route, Routes } from "react-router-dom";
import { TimeRangeQueryStringKey } from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import { RedirectValidation } from "../../utils/routes/redirect-validation/redirect-validation.component";
import { RedirectWithDefaultParams } from "../../utils/routes/redirect-with-default-params/redirect-with-default-params.component";
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
                        <RedirectWithDefaultParams
                            replace
                            to={AppRouteRelative.ANOMALIES_ALL}
                        />
                    }
                />

                {/* Anomalies all path */}
                <Route
                    element={
                        <RedirectValidation
                            queryParams={[
                                TimeRangeQueryStringKey.TIME_RANGE,
                                TimeRangeQueryStringKey.START_TIME,
                                TimeRangeQueryStringKey.END_TIME,
                            ]}
                            to=".."
                        >
                            <AnomaliesAllPage />
                        </RedirectValidation>
                    }
                    path={AppRouteRelative.ANOMALIES_ALL}
                />

                <Route path={`${AppRouteRelative.ALERTS_ALERT}/*`}>
                    {/* Anomalies view index path to default the time range params */}
                    <Route index element={<AnomaliesViewIndexPage />} />

                    {/* Anomalies view path */}
                    <Route
                        element={
                            <RedirectValidation
                                queryParams={[
                                    TimeRangeQueryStringKey.TIME_RANGE,
                                    TimeRangeQueryStringKey.START_TIME,
                                    TimeRangeQueryStringKey.END_TIME,
                                ]}
                                to=".."
                            >
                                <AnomaliesViewPage />
                            </RedirectValidation>
                        }
                        path={AppRouteRelative.ANOMALIES_ANOMALY_VIEW}
                    />

                    {/* No match found, render page not found */}
                    <Route element={<PageNotFoundPage />} path="*" />
                </Route>

                {/* No match found, render page not found */}
                <Route element={<PageNotFoundPage />} path="*" />
            </Routes>
        </Suspense>
    );
};
