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

const AlertTemplatesAllPage = lazy(() =>
    import(
        /* webpackChunkName: "alert-templates-all-page" */ "../../pages/alert-templates-all-page/alert-templates-all-page.component"
    ).then((module) => ({ default: module.AlertTemplatesAllPage }))
);

const AlertTemplatesViewPage = lazy(() =>
    import(
        /* webpackChunkName: "alert-templates-view-page" */ "../../pages/alert-templates-view-page/alert-templates-view-page.component"
    ).then((module) => ({ default: module.AlertTemplatesViewPage }))
);

const AlertTemplatesCreatePage = lazy(() =>
    import(
        /* webpackChunkName: "alert-templates-create-page" */ "../../pages/alert-templates-create-page/alert-templates-create-page.component"
    ).then((module) => ({ default: module.AlertTemplatesCreatePage }))
);

const AlertTemplatesUpdatePage = lazy(() =>
    import(
        /* webpackChunkName: "alert-templates-update-page" */ "../../pages/alert-templates-update-page/alert-templates-update-page.component"
    ).then((module) => ({ default: module.AlertTemplatesUpdatePage }))
);

const PageNotFoundPage = lazy(() =>
    import(
        /* webpackChunkName: "page-not-found-page" */ "../../pages/page-not-found-page/page-not-found-page.component"
    ).then((module) => ({ default: module.PageNotFoundPage }))
);

export const AlertTemplatesRouter: FunctionComponent = () => {
    return (
        <Suspense fallback={<AppLoadingIndicatorV1 />}>
            <Routes>
                {/* AlertTemplates path */}
                {/* Redirect to alert-templates all path */}
                <Route
                    index
                    element={
                        <Navigate
                            replace
                            to={AppRouteRelative.ALERT_TEMPLATES_ALL}
                        />
                    }
                />

                {/* AlertTemplates all path */}
                <Route
                    element={<AlertTemplatesAllPage />}
                    path={AppRouteRelative.ALERT_TEMPLATES_ALL}
                />

                {/* AlertTemplates create path */}
                <Route
                    element={<AlertTemplatesCreatePage />}
                    path={AppRouteRelative.ALERT_TEMPLATES_CREATE}
                />

                {/* AlertTemplates single */}
                <Route
                    path={`${AppRouteRelative.ALERT_TEMPLATES_ALERT_TEMPLATE}/*`}
                >
                    <Route
                        index
                        element={
                            <Navigate
                                replace
                                to={
                                    AppRouteRelative.ALERT_TEMPLATES_ALERT_TEMPLATE_VIEW
                                }
                            />
                        }
                    />

                    {/* AlertTemplates view path */}
                    <Route
                        element={<AlertTemplatesViewPage />}
                        path={
                            AppRouteRelative.ALERT_TEMPLATES_ALERT_TEMPLATE_VIEW
                        }
                    />

                    {/* AlertTemplates update path */}
                    <Route
                        element={<AlertTemplatesUpdatePage />}
                        path={
                            AppRouteRelative.ALERT_TEMPLATES_ALERT_TEMPLATE_UPDATE
                        }
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
