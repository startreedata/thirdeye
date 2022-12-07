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
import React, { FunctionComponent, lazy, Suspense, useEffect } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { useAppBarConfigProvider } from "../../components/app-bar/app-bar-config-provider/app-bar-config-provider.component";
import { PageNotFoundPage } from "../../pages/page-not-found-page/page-not-found-page.component";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import { AppRouteRelative } from "../../utils/routes/routes.util";

const CreateAlertPage = lazy(() =>
    import(
        /* webpackChunkName: "create-alert-page" */ "../../pages/welcome-page/create-alert/create-alert-page.component"
    ).then((module) => ({ default: module.CreateAlertPage }))
);

const SelectTypePage = lazy(() =>
    import(
        /* webpackChunkName: "select-type-page" */ "../../pages/welcome-page/create-alert/select-type/select-type-page.component"
    ).then((module) => ({ default: module.SelectTypePage }))
);

const SetupMonitoringPage = lazy(() =>
    import(
        /* webpackChunkName: "select-monitoring-page" */ "../../pages/welcome-page/create-alert/setup-monitoring/setup-monitoring-page.component"
    ).then((module) => ({ default: module.SetupMonitoringPage }))
);

const SetupDetailsPage = lazy(() =>
    import(
        /* webpackChunkName: "select-details-page" */ "../../pages/welcome-page/create-alert/setup-details/setup-details-page.component"
    ).then((module) => ({ default: module.SetupDetailsPage }))
);

const SetupDimensionGroupsPage = lazy(() =>
    import(
        /* webpackChunkName: "select-dimension-groups-page" */ "../../pages/welcome-page/create-alert/setup-dimension-groups/setup-dimension-groups-page.component"
    ).then((module) => ({ default: module.SetupDimensionGroupsPage }))
);

const WelcomeLandingPage = lazy(() =>
    import(
        /* webpackChunkName: "welcome-landing-page" */ "../../pages/welcome-page/landing/landing-page.component"
    ).then((module) => ({ default: module.WelcomeLandingPage }))
);

const WelcomeOnboardDatasourceWizard = lazy(() =>
    import(
        /* webpackChunkName: "welcome-onboard-datasource" */ "../../pages/welcome-page/create-datasource/create-datasource-page.component"
    ).then((module) => ({ default: module.WelcomeOnboardDatasourceWizard }))
);

const WelcomeSelectDatasource = lazy(() =>
    import(
        /* webpackChunkName: "elect-datasource" */ "../../pages/welcome-page/create-datasource/onboard-datasource/onboard-datasource-page.component"
    ).then((module) => ({ default: module.WelcomeSelectDatasource }))
);

const WelcomeSelectDatasets = lazy(() =>
    import(
        /* webpackChunkName: "select-datasets" */ "../../pages/welcome-page/create-datasource/onboard-datasets/onboard-datasets-page.component"
    ).then((module) => ({ default: module.WelcomeSelectDatasets }))
);

export const WelcomeRouter: FunctionComponent = () => {
    const { setShowAppNavBar } = useAppBarConfigProvider();

    useEffect(() => {
        setShowAppNavBar(false);
    }, []);

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
                    element={<CreateAlertPage />}
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
                        element={<SelectTypePage />}
                        path={AppRouteRelative.WELCOME_CREATE_ALERT_SELECT_TYPE}
                    />

                    {/* Welcome create alert select dimension groups path */}
                    <Route
                        element={<SetupDimensionGroupsPage />}
                        path={
                            AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_DIMENSION_EXPLORATION
                        }
                    />

                    {/* Welcome create alert setup monitoring path */}
                    <Route
                        element={<SetupMonitoringPage />}
                        path={
                            AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_MONITORING
                        }
                    />

                    {/* Welcome create alert setup details path */}
                    <Route
                        element={<SetupDetailsPage />}
                        path={
                            AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_DETAILS
                        }
                    />
                </Route>

                {/* No match found, render page not found */}
                <Route element={<PageNotFoundPage />} path="*" />
            </Routes>
        </Suspense>
    );
};
