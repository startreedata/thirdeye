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
import { Navigate, Route, Routes } from "react-router-dom";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import { AppRouteRelative } from "../../utils/routes/routes.util";
import { AlertsGuidedCreateRouterProps } from "./alerts-guided-create.router.interface";

const CreateAlertPageNewUser = lazy(() =>
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

export const AlertsCreateGuidedRouter: FunctionComponent<AlertsGuidedCreateRouterProps> =
    ({
        sampleAlertsBottom,
        hideHeader,
        hideSampleAlerts,
        hideSubscriptionGroup,
        createLabel,
        inProgressLabel,
    }) => {
        return (
            <Suspense fallback={<AppLoadingIndicatorV1 />}>
                <Routes>
                    <Route
                        element={
                            <CreateAlertPageNewUser hideHeader={hideHeader} />
                        }
                        path="*"
                    >
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
                        <Route
                            element={
                                <SelectTypePage
                                    hideSampleAlerts={hideSampleAlerts}
                                    sampleAlertsBottom={sampleAlertsBottom}
                                />
                            }
                            path={
                                AppRouteRelative.WELCOME_CREATE_ALERT_SELECT_TYPE
                            }
                        />

                        <Route
                            element={<SetupDimensionGroupsPage />}
                            path={
                                AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_DIMENSION_EXPLORATION
                            }
                        />

                        <Route
                            element={<SetupMonitoringPage />}
                            path={
                                AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_MONITORING
                            }
                        />

                        <Route
                            element={
                                <SetupDetailsPage
                                    createLabel={createLabel}
                                    hideSubscriptionGroup={
                                        hideSubscriptionGroup
                                    }
                                    inProgressLabel={inProgressLabel}
                                />
                            }
                            path={
                                AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_DETAILS
                            }
                        />
                    </Route>
                </Routes>
            </Suspense>
        );
    };
