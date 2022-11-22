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
import React, { FunctionComponent, lazy, Suspense } from "react";
import { Navigate, Outlet, Route, Routes } from "react-router-dom";
import { PageNotFoundPage } from "../../pages/page-not-found-page/page-not-found-page.component";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import { AppRouteRelative } from "../../utils/routes/routes.util";

const WelcomeLandingPage = lazy(() =>
    import(
        /* webpackChunkName: "welcome-landing-page" */ "../../pages/welcome-landing-page/welcome-landing-page.component"
    ).then((module) => ({ default: module.WelcomeLandingPage }))
);

const WelcomeOnboardDatasourceWizard = lazy(() =>
    import(
        /* webpackChunkName: "welcome-onboard-datasource" */ "../../pages/welcome-onboard-datasource-wizard/welcome-onboard-datasource-wizard.component"
    ).then((module) => ({ default: module.WelcomeOnboardDatasourceWizard }))
);

const WelcomeSelectDatasource = lazy(() =>
    import(
        /* webpackChunkName: "welcome-select-datasource" */ "../../pages/welcome-onboard-datasource-select-datasource/welcome-onboard-datasource-select-datasource.component"
    ).then((module) => ({ default: module.WelcomeSelectDatasource }))
);

const WelcomeSelectDatasets = lazy(() =>
    import(
        /* webpackChunkName: "welcome-select-datasets" */ "../../pages/welcome-onboard-datasource-select-datasets/welcome-onboard-datasource-select-datasets.component"
    ).then((module) => ({ default: module.WelcomeSelectDatasets }))
);

// * Just setting up the routing, this will be removed by the next commit
const DummyPage: FunctionComponent = () => {
    return (
        <div>
            I am sample page serving the url{" "}
            <kbd>{window.location.pathname}</kbd>
        </div>
    );
};

export const WelcomeRouter: FunctionComponent = () => {
    return (
        <Suspense fallback={<AppLoadingIndicatorV1 />}>
            <Routes>
                {/* Welcome index path */}
                {/* Redirect to metrics all path */}
                <Route
                    index
                    element={
                        <Navigate
                            replace
                            to={AppRouteRelative.WELCOME_LANDING}
                        />
                    }
                />

                {/* Welcome landing path */}
                <Route
                    element={<WelcomeLandingPage />}
                    path={AppRouteRelative.WELCOME_LANDING}
                />

                {/* Welcome onboard-datasource path */}
                <Route
                    element={<WelcomeOnboardDatasourceWizard />}
                    path={`${AppRouteRelative.WELCOME_ONBOARD_DATASOURCE}/*`}
                >
                    {/* Welcome onboard-datasource index path */}
                    {/* Redirect to onboard-datasource datasource path */}
                    <Route
                        index
                        element={
                            <Navigate
                                replace
                                to={
                                    AppRouteRelative.WELCOME_ONBOARD_DATASOURCE_DATASOURCE
                                }
                            />
                        }
                    />

                    {/* Welcome onboard-datasource datasource path */}
                    <Route
                        element={<WelcomeSelectDatasource />}
                        path={
                            AppRouteRelative.WELCOME_ONBOARD_DATASOURCE_DATASOURCE
                        }
                    />

                    {/* Welcome onboard-datasource datasets path */}
                    <Route
                        element={<WelcomeSelectDatasets />}
                        path={
                            AppRouteRelative.WELCOME_ONBOARD_DATASOURCE_DATASETS
                        }
                    />

                    <Route
                        element={
                            <Navigate
                                replace
                                to={
                                    AppRouteRelative.WELCOME_ONBOARD_DATASOURCE_DATASOURCE
                                }
                            />
                        }
                        path="*"
                    />
                </Route>

                {/* Welcome create alert path */}
                <Route
                    element={<Outlet />}
                    path={`${AppRouteRelative.WELCOME_CREATE_ALERT}/*`}
                >
                    {/* Welcome create alert index path */}
                    {/* Redirect to create alert select type path */}
                    <Route
                        index
                        element={
                            <Navigate
                                replace
                                to={
                                    AppRouteRelative.WELCOME_CREATE_ALERT_SELECT_TYPE
                                }
                            />
                        }
                    />

                    {/* Welcome create alert select type path */}
                    <Route
                        element={<DummyPage />}
                        path={AppRouteRelative.WELCOME_CREATE_ALERT_SELECT_TYPE}
                    />

                    {/* Welcome create alert setup monitoring path */}
                    <Route
                        element={<DummyPage />}
                        path={
                            AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_MONITORING
                        }
                    />
                </Route>

                {/* No match found, render page not found */}
                <Route element={<PageNotFoundPage />} path="*" />
            </Routes>
        </Suspense>
    );
};
