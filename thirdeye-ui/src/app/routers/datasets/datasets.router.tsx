/*
 * Copyright 2022 StarTree Inc
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
import { default as React, FunctionComponent, lazy, Suspense } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import { AppRouteRelative } from "../../utils/routes/routes.util";

const DatasetsAllPage = lazy(() =>
    import(
        /* webpackChunkName: "datasets-all-page" */ "../../pages/datasets-all-page/datasets-all-page.component"
    ).then((module) => ({ default: module.DatasetsAllPage }))
);

const DatasetsViewPage = lazy(() =>
    import(
        /* webpackChunkName: "datasets-view-page" */ "../../pages/datasets-view-page/datasets-view-page.component"
    ).then((module) => ({ default: module.DatasetsViewPage }))
);

const DatasetsOnboardPage = lazy(() =>
    import(
        /* webpackChunkName: "datasets-onboard-page" */ "../../pages/datasets-onboard-page/datasets-onboard-page.component"
    ).then((module) => ({ default: module.DatasetsOnboardPage }))
);

const DatasetsUpdatePage = lazy(() =>
    import(
        /* webpackChunkName: "datasets-update-page" */ "../../pages/datasets-update-page/datasets-update-page.component"
    ).then((module) => ({ default: module.DatasetsUpdatePage }))
);

const PageNotFoundPage = lazy(() =>
    import(
        /* webpackChunkName: "page-not-found-page" */ "../../pages/page-not-found-page/page-not-found-page.component"
    ).then((module) => ({ default: module.PageNotFoundPage }))
);

export const DatasetsRouter: FunctionComponent = () => {
    return (
        <Suspense fallback={<AppLoadingIndicatorV1 />}>
            <Routes>
                {/* Datasets path */}
                {/* Redirect to datasets all path */}
                <Route
                    index
                    element={
                        <Navigate replace to={AppRouteRelative.DATASETS_ALL} />
                    }
                />

                {/* Datasets all path */}
                <Route
                    element={<DatasetsAllPage />}
                    path={AppRouteRelative.DATASETS_ALL}
                />

                {/* Datasets view path */}
                <Route
                    element={<DatasetsViewPage />}
                    path={AppRouteRelative.DATASETS_VIEW}
                />

                {/* Datasets onboard path */}
                <Route
                    element={<DatasetsOnboardPage />}
                    path={AppRouteRelative.DATASETS_ONBOARD}
                />

                {/* Datasets update path */}
                <Route
                    element={<DatasetsUpdatePage />}
                    path={AppRouteRelative.DATASETS_UPDATE}
                />

                {/* No match found, render page not found */}
                <Route element={<PageNotFoundPage />} path="*" />
            </Routes>
        </Suspense>
    );
};
