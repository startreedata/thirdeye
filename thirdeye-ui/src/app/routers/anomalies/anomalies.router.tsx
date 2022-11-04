// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import { default as React, FunctionComponent, lazy, Suspense } from "react";
import { Navigate, Route, Routes, useLocation } from "react-router-dom";
import { TimeRangeQueryStringKey } from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import { AnomaliesListAllPage } from "../../pages/anomalies-list-page/anomalies-list-page.component";
import { MetricsReportAllPage } from "../../pages/anomalies-metrics-report-page/anomalies-metrics-report-page.component";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import { RedirectValidation } from "../../utils/routes/redirect-validation/redirect-validation.component";
import { RedirectWithDefaultParams } from "../../utils/routes/redirect-with-default-params/redirect-with-default-params.component";
import {
    AppRouteRelative,
    generateDateRangeMonthsFromNow,
} from "../../utils/routes/routes.util";
import { SaveLastUsedSearchParams } from "../../utils/routes/save-last-used-search-params/save-last-used-search-params.component";

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
    const { search } = useLocation();

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
                <Route path={AppRouteRelative.ANOMALIES_ALL}>
                    <Route
                        index
                        element={
                            <RedirectWithDefaultParams
                                replace
                                useStoredLastUsedParamsPathKey
                                customDurationGenerator={() => {
                                    return generateDateRangeMonthsFromNow(3);
                                }}
                                to={AppRouteRelative.ANOMALIES_ALL_RANGE}
                            />
                        }
                    />
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
                                <SaveLastUsedSearchParams>
                                    <AnomaliesAllPage />
                                </SaveLastUsedSearchParams>
                            </RedirectValidation>
                        }
                        path={AppRouteRelative.ANOMALIES_ALL_RANGE}
                    >
                        <Route
                            index
                            element={
                                /**
                                 * Navigate will strip away the query params
                                 * so we have to pass it
                                 */
                                <Navigate
                                    replace
                                    to={`${AppRouteRelative.ANOMALIES_LIST}${search}`}
                                />
                            }
                        />
                        <Route
                            element={<AnomaliesListAllPage />}
                            path={AppRouteRelative.ANOMALIES_LIST}
                        />
                        <Route
                            element={<MetricsReportAllPage />}
                            path={AppRouteRelative.ANOMALIES_METRICS_REPORT}
                        />
                    </Route>
                </Route>

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
