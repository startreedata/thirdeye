/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import React, { FunctionComponent, lazy, Suspense } from "react";
import { useTranslation } from "react-i18next";
import { Navigate, Outlet, Route, Routes } from "react-router-dom";
import { CancelAPICallsOnPageUnload } from "../../components/cancel-api-calls-on-page-unload/cancel-api-calls-on-page-unload.component";
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

const AlertsCreateUpdated = lazy(() =>
    import(
        /* webpackChunkName: "alerts-all-page" */ "../../pages/create-alert"
    ).then((module) => ({ default: module.CreateAlert }))
);

const AlertsAllPage = lazy(() =>
    import(
        /* webpackChunkName: "alerts-all-page" */ "../../pages/alerts-all-page-v2/alerts-all-page.component"
    ).then((module) => ({ default: module.AlertsAllPage }))
);

const AlertsViewPage = lazy(() =>
    import(
        /* webpackChunkName: "alerts-view-page" */ "../../pages/alerts-view-page/alerts-view-page.component"
    ).then((module) => ({ default: module.AlertsViewPage }))
);

const AlertsViewPageV2 = lazy(() =>
    import(
        /* webpackChunkName: "alerts-view-page" */ "../../pages/alerts-view-page-v2/alerts-view-page-v2.component"
    ).then((module) => ({ default: module.AlertsViewPageV2 }))
);

const AlertsAnomaliesPage = lazy(() =>
    import(
        /* webpackChunkName: "alerts-view-page" */ "../../pages/alerts-anomalies-page/alerts-anomalies-page.component"
    ).then((module) => ({ default: module.AlertsAnomaliesPage }))
);

const AlertsCreateJSONPage = lazy(() =>
    import(
        /* webpackChunkName: "alerts-create-json-page" */ "../../pages/alerts-create-page/alerts-create-json-page/alerts-create-json-page.component"
    ).then((module) => ({ default: module.AlertsCreateJSONPage }))
);

const AlertsCreateJSONPageV2 = lazy(() =>
    import(
        /* webpackChunkName: "alerts-create-json-page" */ "../../pages/alerts-create-page/alerts-create-json-page-v2/alerts-create-json-page-v2.component"
    ).then((module) => ({ default: module.AlertsCreateJSONPage }))
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

const AlertsCreateAdvancedPage = lazy(() =>
    import(
        /* webpackChunkName: "alerts-create-advanced-page" */ "../../pages/alerts-create-page/alerts-create-advanced-page/alerts-create-advanced-page.component"
    ).then((module) => ({ default: module.AlertsCreateAdvancedPage }))
);

const AlertsCreateAdvancedPageV2 = lazy(() =>
    import(
        /* webpackChunkName: "alerts-create-advanced-page" */ "../../pages/alerts-create-page/alerts-create-advanced-page-v2/alerts-create-advanced-page.component"
    ).then((module) => ({ default: module.AlertsCreateAdvancedPage }))
);

const AlertsUpdateJSONPage = lazy(() =>
    import(
        /* webpackChunkName: "alerts-update-json-page" */ "../../pages/alerts-update-page/alerts-update-json-page.component"
    ).then((module) => ({ default: module.AlertsUpdateJSONPage }))
);

const AlertsUpdateAdvancedPage = lazy(() =>
    import(
        /* webpackChunkName: "alerts-update-advanced-page" */ "../../pages/alerts-update-page/alerts-update-advanced-page.component"
    ).then((module) => ({ default: module.AlertsUpdateAdvancedPage }))
);

const AlertsUpdateAdvancedPageV2 = lazy(() =>
    import(
        /* webpackChunkName: "alerts-update-advanced-page" */ "../../pages/alerts-update-page/alerts-update-advanced-page-v2.component"
    ).then((module) => ({ default: module.AlertsUpdateAdvancedPage }))
);

const PageNotFoundPage = lazy(() =>
    import(
        /* webpackChunkName: "page-not-found-page" */ "../../pages/page-not-found-page/page-not-found-page.component"
    ).then((module) => ({ default: module.PageNotFoundPage }))
);

const AlertsCreateGuidedRouter = lazy(() =>
    import(
        /* webpackChunkName: "alerts-guided-create" */ "../alerts-guided-create/alerts-guided-create.router"
    ).then((module) => ({ default: module.AlertsCreateGuidedRouter }))
);

const AlertsCreateEasyPage = lazy(() =>
    import(
        /* webpackChunkName: "alerts-create-advanced-page" */ "../../pages/alerts-create-page/alerts-create-easy-page/alerts-create-easy-page.component"
    ).then((module) => ({ default: module.AlertsCreateEasyPage }))
);
// const AlertsCreateEasyPage = lazy(() =>
//     import(
//         /* webpackChunkName: "alerts-create-easy-page" */ "../../pages/create-alert/"
//     ).then((module) => ({ default: module.CreateAlert }))
// );

const CreateAlertGuidedPage = lazy(() =>
    import(
        /* webpackChunkName: "create-alert-guided-page" */ "../../pages/alerts-create-guided-page/alerts-create-guided-page.component"
    ).then((module) => ({ default: module.CreateAlertGuidedPage }))
);

export const AlertsRouter: FunctionComponent = () => {
    const { t } = useTranslation();

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
                    element={
                        <CancelAPICallsOnPageUnload
                            key={AppRouteRelative.ALERTS_ALL}
                        >
                            <AlertsAllPage />
                        </CancelAPICallsOnPageUnload>
                    }
                    path={AppRouteRelative.ALERTS_ALL}
                />

                {/* Alerts create path */}
                <Route path={`${AppRouteRelative.ALERTS_CREATE}/*`}>
                    <Route
                        element={
                            <CancelAPICallsOnPageUnload
                                key={AppRouteRelative.ALERTS_ALL}
                            >
                                <AlertsCreateUpdated />
                            </CancelAPICallsOnPageUnload>
                        }
                        path="easy"
                    />
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
                        element={
                            <CancelAPICallsOnPageUnload
                                key={`${AppRouteRelative.ALERTS_CREATE_NEW}/*`}
                            >
                                <AlertsCreatePageNew />
                            </CancelAPICallsOnPageUnload>
                        }
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
                                    to={AppRouteRelative.ALERTS_CREATE_NEW_USER}
                                />
                            }
                        />

                        <Route
                            element={
                                <CancelAPICallsOnPageUnload
                                    key={`${AppRouteRelative.ALERTS_CREATE_NEW_USER}/*`}
                                >
                                    <AlertsCreateGuidedRouter
                                        createLabel={t("label.create")}
                                        inProgressLabel={t("label.creating")}
                                    />
                                </CancelAPICallsOnPageUnload>
                            }
                            path={`${AppRouteRelative.ALERTS_CREATE_NEW_USER}/*`}
                        />

                        <Route
                            element={<AlertsCreateAdvancedPage />}
                            path={AppRouteRelative.ALERTS_CREATE_ADVANCED}
                        />

                        <Route
                            element={<AlertsCreateAdvancedPageV2 />}
                            path={AppRouteRelative.ALERTS_CREATE_ADVANCED_V2}
                        />
                        <Route
                            element={<AlertsCreateJSONPage />}
                            path={AppRouteRelative.ALERTS_CREATE_JSON_EDITOR}
                        />
                        <Route
                            element={<AlertsCreateJSONPageV2 />}
                            path={AppRouteRelative.ALERTS_CREATE_JSON_EDITOR_V2}
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
                                    to={
                                        AppRouteRelative.ALERTS_CREATE_JSON_EDITOR
                                    }
                                />
                            }
                        />

                        <Route
                            element={
                                <AlertsCreateGuidedRouter
                                    createLabel={t("label.create")}
                                    inProgressLabel={t("label.creating")}
                                />
                            }
                            path={`${AppRouteRelative.ALERTS_CREATE_NEW_USER}/*`}
                        />

                        <Route
                            element={<AlertsCreateAdvancedPage />}
                            path={AppRouteRelative.ALERTS_CREATE_ADVANCED}
                        />
                        <Route
                            element={<AlertsCreateAdvancedPageV2 />}
                            path={AppRouteRelative.ALERTS_CREATE_ADVANCED_V2}
                        />
                        <Route
                            element={<AlertsCreateJSONPage />}
                            path={AppRouteRelative.ALERTS_CREATE_JSON_EDITOR}
                        />
                        <Route
                            element={<AlertsCreateJSONPageV2 />}
                            path={AppRouteRelative.ALERTS_CREATE_JSON_EDITOR_V2}
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
                        element={
                            <RedirectValidation
                                queryParams={[
                                    TimeRangeQueryStringKey.START_TIME,
                                    TimeRangeQueryStringKey.END_TIME,
                                ]}
                                to=".."
                            >
                                <AlertsViewPageV2 />
                            </RedirectValidation>
                        }
                        path={AppRouteRelative.ALERTS_VIEW_V2}
                    />

                    <Route
                        element={<AlertsUpdateBasePage />}
                        path={AppRouteRelative.ALERTS_UPDATE}
                    >
                        <Route
                            element={<CreateAlertGuidedPage />}
                            path={AppRouteRelative.ALERTS_UPDATE_SIMPLE}
                        >
                            <Route
                                element={<AlertsCreateEasyPage />}
                                path={AppRouteRelative.ALERTS_CREATE_EASY_ALERT}
                            />
                        </Route>
                        <Route
                            index
                            element={
                                <RedirectWithDefaultParams
                                    customDurationGenerator={() => {
                                        return generateDateRangeMonthsFromNow(
                                            1
                                        );
                                    }}
                                    to={
                                        AppRouteRelative.ALERTS_UPDATE_JSON_EDITOR
                                    }
                                />
                            }
                        />

                        <Route
                            element={
                                <AlertsCreateGuidedRouter
                                    createLabel={t("label.update-entity", {
                                        entity: t("label.alert"),
                                    })}
                                    inProgressLabel={t("label.updating")}
                                />
                            }
                            path={`${AppRouteRelative.ALERTS_CREATE_NEW_USER}/*`}
                        />

                        <Route
                            element={<AlertsUpdateAdvancedPage />}
                            path={AppRouteRelative.ALERTS_UPDATE_ADVANCED}
                        />
                        <Route
                            element={<AlertsUpdateAdvancedPageV2 />}
                            path={AppRouteRelative.ALERTS_UPDATE_ADVANCED_V2}
                        />
                        <Route
                            element={<AlertsUpdateJSONPage />}
                            path={AppRouteRelative.ALERTS_UPDATE_JSON_EDITOR}
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
