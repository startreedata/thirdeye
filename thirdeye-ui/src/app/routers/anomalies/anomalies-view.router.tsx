/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { default as React, FunctionComponent, lazy, Suspense } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import { AppRouteRelative } from "../../utils/routes/routes.util";

const AnomaliesListRouter = lazy(() =>
    import(
        /* webpackChunkName: "anomalies-list-router" */ "../anomalies-list/anomalies-list.router"
    ).then((module) => ({ default: module.AnomaliesListRouter }))
);

const MetricsReportRouter = lazy(() =>
    import(
        /* webpackChunkName: "metrics-report" */ "../metrics-report/metrics-report.router"
    ).then((module) => ({ default: module.MetricsReportRouter }))
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
                {/* anomalies path */}
                <Route
                    index
                    element={
                        <Navigate
                            replace
                            to={AppRouteRelative.ANOMALIES_LIST}
                        />
                    }
                />

                {/* Direct all anomalies paths to anomalies router */}
                <Route
                    element={<AnomaliesListRouter />}
                    path={`${AppRouteRelative.ANOMALIES_LIST}/*`}
                />

                {/* Direct all metrics report paths to metrics report router */}
                <Route
                    element={<MetricsReportRouter />}
                    path={`${AppRouteRelative.METRICS_REPORT}/*`}
                />

                {/* No match found, render page not found */}
                <Route element={<PageNotFoundPage />} path="*" />
            </Routes>
        </Suspense>
    );
};
