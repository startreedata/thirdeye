// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import React, { FunctionComponent, lazy, Suspense } from "react";
import { Navigate, Outlet, Route, Routes } from "react-router-dom";
import { TimeRangeQueryStringKey } from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import { AlertsUpdateBasePage } from "../../pages/alerts-update-page/alerts-update-base-page.component";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import { AlertFetchRedirectParams } from "../../utils/routes/alert-fetch-redirect-params/alert-fetch-redirect-params.component";
import { RedirectValidation } from "../../utils/routes/redirect-validation/redirect-validation.component";
import { RedirectWithDefaultParams } from "../../utils/routes/redirect-with-default-params/redirect-with-default-params.component";
import {
    AppRouteRelative,
    generateDateRangeMonthsFromNow,
} from "../../utils/routes/routes.util";

const AlertsAllPage = lazy(() =>
    import(
        /* webpackChunkName: "alerts-all-page" */ "../../pages/alerts-all-page/alerts-all-page.component"
    ).then((module) => ({ default: module.AlertsAllPage }))
);

const AlertsViewPage = lazy(() =>
    import(
        /* webpackChunkName: "alerts-view-page" */ "../../pages/alerts-view-page/alerts-view-page.component"
    ).then((module) => ({ default: module.AlertsViewPage }))
);

const AlertsAnomaliesPage = lazy(() =>
    import(
        /* webpackChunkName: "alerts-view-page" */ "../../pages/alerts-anomalies-page/alerts-anomalies-page.component"
    ).then((module) => ({ default: module.AlertsAnomaliesPage }))
);

const AlertsCreateAdvancePage = lazy(() =>
    import(
        /* webpackChunkName: "alerts-create-advanced-page" */ "../../pages/alerts-create-page/alerts-create-advance-page/alerts-create-advance-page.component"
    ).then((module) => ({ default: module.AlertsCreateAdvancePage }))
);

const AlertsCreatePageNew = lazy(() =>
    import(
        /* webpackChunkName: "alerts-create-new-page" */ "../../pages/alerts-create-page/alerts-create-new-page.component"
    ).then((module) => ({ default: module.AlertsCreateNewPage }))
);

const AlertsCreatePageCopy = lazy(() =>
    import(
        /* webpackChunkName: "alerts-create-copy-page" */ "../../pages/alerts-create-page/alerts-create-copy-page.component"
    ).then((module) => ({ default: module.AlertsCreateCopyPage }))
);

const AlertsCreateSimplePage = lazy(() =>
    import(
        /* webpackChunkName: "alerts-create-page" */ "../../pages/alerts-create-page/alerts-create-simple-page/alerts-create-simple-page.component"
    ).then((module) => ({ default: module.AlertsCreateSimplePage }))
);

const AlertsUpdateAdvancedPage = lazy(() =>
    import(
        /* webpackChunkName: "alerts-update-advanced-page" */ "../../pages/alerts-update-page/alerts-update-advanced-page.component"
    ).then((module) => ({ default: module.AlertsUpdateAdvancedPage }))
);

const AlertsUpdateSimplePage = lazy(() =>
    import(
        /* webpackChunkName: "alerts-update-simple-page" */ "../../pages/alerts-update-page/alerts-update-simple-page.component"
    ).then((module) => ({ default: module.AlertsUpdateSimplePage }))
);

const PageNotFoundPage = lazy(() =>
    import(
        /* webpackChunkName: "page-not-found-page" */ "../../pages/page-not-found-page/page-not-found-page.component"
    ).then((module) => ({ default: module.PageNotFoundPage }))
);

export const AlertsRouter: FunctionComponent = () => {
    return (
        <Suspense fallback={<AppLoadingIndicatorV1 />}>
            <Routes>
                {/* Alerts path */}
                {/* Redirect to alerts all path */}
                <Route
                    index
                    element={
                        <Navigate replace to={AppRouteRelative.ALERTS_ALL} />
                    }
                />

                {/* Alerts all path */}
                <Route
                    element={<AlertsAllPage />}
                    path={AppRouteRelative.ALERTS_ALL}
                />

                {/* Alerts create path */}
                <Route path={`${AppRouteRelative.ALERTS_CREATE}/*`}>
                    <Route
                        index
                        element={
                            <Navigate
                                replace
                                to={AppRouteRelative.ALERTS_CREATE_NEW}
                            />
                        }
                    />

                    <Route
                        element={<AlertsCreatePageNew />}
                        path={`${AppRouteRelative.ALERTS_CREATE_NEW}/*`}
                    >
                        <Route
                            index
                            element={
                                <RedirectWithDefaultParams
                                    customDurationGenerator={() => {
                                        return generateDateRangeMonthsFromNow(
                                            1
                                        );
                                    }}
                                    to={AppRouteRelative.ALERTS_CREATE_ADVANCED}
                                />
                            }
                        />

                        <Route
                            element={<AlertsCreateSimplePage />}
                            path={AppRouteRelative.ALERTS_CREATE_SIMPLE}
                        />
                        <Route
                            element={<AlertsCreateAdvancePage />}
                            path={AppRouteRelative.ALERTS_CREATE_ADVANCED}
                        />
                    </Route>

                    <Route
                        element={<AlertsCreatePageCopy />}
                        path={`${AppRouteRelative.ALERTS_CREATE_COPY}/*`}
                    >
                        <Route
                            index
                            element={
                                <RedirectWithDefaultParams
                                    customDurationGenerator={() => {
                                        return generateDateRangeMonthsFromNow(
                                            1
                                        );
                                    }}
                                    to={AppRouteRelative.ALERTS_CREATE_ADVANCED}
                                />
                            }
                        />

                        <Route
                            element={<AlertsCreateSimplePage />}
                            path={AppRouteRelative.ALERTS_CREATE_SIMPLE}
                        />
                        <Route
                            element={<AlertsCreateAdvancePage />}
                            path={AppRouteRelative.ALERTS_CREATE_ADVANCED}
                        />
                    </Route>

                    {/* No match found, render page not found */}
                    <Route element={<PageNotFoundPage />} path="*" />
                </Route>

                {/* Alert paths */}
                <Route
                    element={<Outlet />}
                    path={`${AppRouteRelative.ALERTS_ALERT}/*`}
                >
                    <Route
                        index
                        element={
                            <AlertFetchRedirectParams
                                fallbackDurationGenerator={() => {
                                    return generateDateRangeMonthsFromNow(1);
                                }}
                                to={AppRouteRelative.ALERTS_VIEW}
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
                                <AlertsAnomaliesPage />
                            </RedirectValidation>
                        }
                        path={AppRouteRelative.ALERTS_ANOMALIES}
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
                                <AlertsViewPage />
                            </RedirectValidation>
                        }
                        path={AppRouteRelative.ALERTS_VIEW}
                    />

                    <Route
                        element={<AlertsUpdateBasePage />}
                        path={AppRouteRelative.ALERTS_UPDATE}
                    >
                        <Route
                            index
                            element={
                                <RedirectWithDefaultParams
                                    customDurationGenerator={() => {
                                        return generateDateRangeMonthsFromNow(
                                            1
                                        );
                                    }}
                                    to={AppRouteRelative.ALERTS_UPDATE_ADVANCED}
                                />
                            }
                        />

                        <Route
                            element={<AlertsUpdateSimplePage />}
                            path={AppRouteRelative.ALERTS_UPDATE_SIMPLE}
                        />
                        <Route
                            element={<AlertsUpdateAdvancedPage />}
                            path={AppRouteRelative.ALERTS_UPDATE_ADVANCED}
                        />
                    </Route>

                    {/* No match found, render page not found */}
                    <Route element={<PageNotFoundPage />} path="*" />
                </Route>

                {/* No match found, render page not found */}
                <Route element={<PageNotFoundPage />} path="*" />
            </Routes>
        </Suspense>
    );
};
