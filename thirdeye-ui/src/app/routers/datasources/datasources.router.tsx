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
import { AppLoadingIndicatorV1 } from "@startree-ui/platform-ui";
import { default as React, FunctionComponent, lazy, Suspense } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { AppRouteRelative } from "../../utils/routes/routes.util";

const DatasourcesAllPage = lazy(() =>
    import(
        /* webpackChunkName: "datasources-all-page" */ "../../pages/datasources-all-page/datasources-all-page.component"
    ).then((module) => ({ default: module.DatasourcesAllPage }))
);

const DatasourcesViewPage = lazy(() =>
    import(
        /* webpackChunkName: "datasources-view-page" */ "../../pages/datasources-view-page/datasources-view-page.component"
    ).then((module) => ({ default: module.DatasourcesViewPage }))
);

const DatasourcesCreatePage = lazy(() =>
    import(
        /* webpackChunkName: "datasources-create-page" */ "../../pages/datasources-create-page/datasources-create-page.component"
    ).then((module) => ({ default: module.DatasourcesCreatePage }))
);

const DatasourcesUpdatePage = lazy(() =>
    import(
        /* webpackChunkName: "datasources-update-page" */ "../../pages/datasources-update-page/datasources-update-page.component"
    ).then((module) => ({ default: module.DatasourcesUpdatePage }))
);

const PageNotFoundPage = lazy(() =>
    import(
        /* webpackChunkName: "page-not-found-page" */ "../../pages/page-not-found-page/page-not-found-page.component"
    ).then((module) => ({ default: module.PageNotFoundPage }))
);

export const DatasourcesRouter: FunctionComponent = () => {
    return (
        <Suspense fallback={<AppLoadingIndicatorV1 />}>
            <Routes>
                {/* Datasources path */}
                {/* Redirect to datasources all path */}
                <Route
                    index
                    element={
                        <Navigate
                            replace
                            to={AppRouteRelative.DATASOURCES_ALL}
                        />
                    }
                />

                {/* Datasources all path */}
                <Route
                    element={<DatasourcesAllPage />}
                    path={AppRouteRelative.DATASOURCES_ALL}
                />

                {/* Datasources view path */}
                <Route
                    element={<DatasourcesViewPage />}
                    path={AppRouteRelative.DATASOURCES_VIEW}
                />

                {/* Datasources create path */}
                <Route
                    element={<DatasourcesCreatePage />}
                    path={AppRouteRelative.DATASOURCES_CREATE}
                />

                {/* Datasources update path */}
                <Route
                    element={<DatasourcesUpdatePage />}
                    path={AppRouteRelative.DATASOURCES_UPDATE}
                />

                {/* No match found, render page not found */}
                <Route element={<PageNotFoundPage />} path="*" />
            </Routes>
        </Suspense>
    );
};
