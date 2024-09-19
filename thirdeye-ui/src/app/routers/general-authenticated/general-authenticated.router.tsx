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
import { Navigate, Route, Routes } from "react-router-dom";
import { CancelAPICallsOnPageUnload } from "../../components/cancel-api-calls-on-page-unload/cancel-api-calls-on-page-unload.component";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import {
    AppRoute,
    AppRouteRelative,
    getBasePath,
    getHomePath,
} from "../../utils/routes/routes.util";

const HomePage = lazy(() =>
    import(
        /* webpackChunkName: "home-page" */ "../../pages/home-page/home-page.component"
    ).then((module) => ({ default: module.HomePage }))
);

const AdminPage = lazy(() =>
    import(
        /* webpackChunkName: "admin-page" */ "../../pages/admin-page/admin-page.component"
    ).then((module) => ({ default: module.AdminPage }))
);

const LogoutPage = lazy(() =>
    import(
        /* webpackChunkName: "logout-page" */ "../../pages/logout-page/logout-page.component"
    ).then((module) => ({ default: module.LogoutPage }))
);

const PageNotFoundPage = lazy(() =>
    import(
        /* webpackChunkName: "page-not-found-page" */ "../../pages/page-not-found-page/page-not-found-page.component"
    ).then((module) => ({ default: module.PageNotFoundPage }))
);

const SwaggerDocs = lazy(() =>
    import(/* webpackChunkName: "swagger-page" */ "../../pages/swagger").then(
        (module) => ({ default: module.SwaggerDocs })
    )
);

export const GeneralAuthenticatedRouter: FunctionComponent = () => {
    return (
        <Suspense fallback={<AppLoadingIndicatorV1 />}>
            <Routes>
                {/* Base path */}
                {/* Redirect to home path */}
                <Route
                    element={<Navigate replace to={getHomePath()} />}
                    path={AppRoute.BASE}
                />
                <Route element={<SwaggerDocs />} path={AppRoute.SWAGGER} />

                {/* Home path */}
                <Route
                    element={
                        <CancelAPICallsOnPageUnload key={AppRouteRelative.HOME}>
                            <HomePage />
                        </CancelAPICallsOnPageUnload>
                    }
                    path={`${AppRouteRelative.HOME}`}
                />

                {/* Admin path */}
                <Route
                    element={
                        <CancelAPICallsOnPageUnload
                            key={AppRouteRelative.ADMIN}
                        >
                            <AdminPage />
                        </CancelAPICallsOnPageUnload>
                    }
                    path={`${AppRouteRelative.ADMIN}`}
                />

                {/* Login path */}
                {/* Already authenticated, redirect to base path */}
                <Route
                    element={<Navigate replace to={getBasePath()} />}
                    path={AppRouteRelative.LOGIN}
                />

                {/* Logout path */}
                <Route
                    element={<LogoutPage />}
                    path={AppRouteRelative.LOGOUT}
                />

                {/* No match found, render page not found */}
                <Route element={<PageNotFoundPage />} path="*" />
            </Routes>
        </Suspense>
    );
};
