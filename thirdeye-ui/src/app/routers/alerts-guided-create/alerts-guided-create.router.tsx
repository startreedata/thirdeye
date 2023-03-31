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
import { CancelAPICallsOnPageUnload } from "../../components/cancel-api-calls-on-page-unload/cancel-api-calls-on-page-unload.component";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import { AppRouteRelative } from "../../utils/routes/routes.util";
import { AlertsGuidedCreateRouterProps } from "./alerts-guided-create.router.interface";

const CreateAlertPageNewUser = lazy(() =>
    import(
        /* webpackChunkName: "create-alert-new-user-page" */ "../../pages/alerts-create-page/create-alert-new-user-page.component"
    ).then((module) => ({ default: module.CreateAlertNewUserPage }))
);

const CreateAlertWelcomeFlow = lazy(() =>
    import(
        /* webpackChunkName: "create-alert-welcome-flow-page" */ "../../pages/welcome-page/create-alert/create-alert-page.component"
    ).then((module) => ({ default: module.CreateAlertPage }))
);

const SelectTypePage = lazy(() =>
    import(
        /* webpackChunkName: "select-type-page" */ "../../pages/alerts-create-guided-page/select-type/select-type-page.component"
    ).then((module) => ({ default: module.SelectTypePage }))
);

const SetupMonitoringPage = lazy(() =>
    import(
        /* webpackChunkName: "setup-monitoring-page" */ "../../pages/alerts-create-guided-page/setup-monitoring/setup-monitoring-page.component"
    ).then((module) => ({ default: module.SetupMonitoringPage }))
);

const SetupDetailsPage = lazy(() =>
    import(
        /* webpackChunkName: "setup-details-page" */ "../../pages/alerts-create-guided-page/setup-details/setup-details-page.component"
    ).then((module) => ({ default: module.SetupDetailsPage }))
);

const SetupAnomaliesFilterPage = lazy(() =>
    import(
        /* webpackChunkName: "setup-anomalies-filter-page" */ "../../pages/alerts-create-guided-page/setup-anomalies-filter/setup-anomalies-filter-page.component"
    ).then((module) => ({ default: module.SetupAnomaliesFilterPage }))
);

const SetupDimensionGroupsPage = lazy(() =>
    import(
        /* webpackChunkName: "setup-dimension-groups-page" */ "../../pages/alerts-create-guided-page/setup-dimension-groups/setup-dimension-groups-page.component"
    ).then((module) => ({ default: module.SetupDimensionGroupsPage }))
);

export const AlertsCreateGuidedRouter: FunctionComponent<AlertsGuidedCreateRouterProps> =
    ({
        useParentForNonWelcomeFlow,
        sampleAlertsBottom,
        hideSampleAlerts,
        createLabel,
        inProgressLabel,
        hideCurrentlySelected,
        navigateToAlertDetailAfterSampleAlertCreate,
    }) => {
        return (
            <Suspense fallback={<AppLoadingIndicatorV1 />}>
                <Routes>
                    <Route
                        element={
                            useParentForNonWelcomeFlow ? (
                                <CreateAlertPageNewUser />
                            ) : (
                                <CreateAlertWelcomeFlow />
                            )
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
                                <CancelAPICallsOnPageUnload
                                    key={
                                        AppRouteRelative.WELCOME_CREATE_ALERT_SELECT_TYPE
                                    }
                                >
                                    <SelectTypePage
                                        hideCurrentlySelected={
                                            hideCurrentlySelected
                                        }
                                        hideSampleAlerts={hideSampleAlerts}
                                        navigateToAlertDetailAfterCreate={
                                            navigateToAlertDetailAfterSampleAlertCreate
                                        }
                                        sampleAlertsBottom={sampleAlertsBottom}
                                    />
                                </CancelAPICallsOnPageUnload>
                            }
                            path={
                                AppRouteRelative.WELCOME_CREATE_ALERT_SELECT_TYPE
                            }
                        />

                        <Route
                            element={
                                <CancelAPICallsOnPageUnload
                                    key={
                                        AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_DIMENSION_EXPLORATION
                                    }
                                >
                                    <SetupDimensionGroupsPage />
                                </CancelAPICallsOnPageUnload>
                            }
                            path={
                                AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_DIMENSION_EXPLORATION
                            }
                        />

                        <Route
                            element={
                                <CancelAPICallsOnPageUnload
                                    key={
                                        AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_MONITORING
                                    }
                                >
                                    <SetupMonitoringPage />
                                </CancelAPICallsOnPageUnload>
                            }
                            path={
                                AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_MONITORING
                            }
                        />

                        <Route
                            element={
                                <CancelAPICallsOnPageUnload
                                    key={
                                        AppRouteRelative.WELCOME_CREATE_ALERT_ANOMALIES_FILTER
                                    }
                                >
                                    <SetupAnomaliesFilterPage />
                                </CancelAPICallsOnPageUnload>
                            }
                            path={
                                AppRouteRelative.WELCOME_CREATE_ALERT_ANOMALIES_FILTER
                            }
                        />

                        <Route
                            element={
                                <CancelAPICallsOnPageUnload
                                    key={
                                        AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_DETAILS
                                    }
                                >
                                    <SetupDetailsPage
                                        createLabel={createLabel}
                                        inProgressLabel={inProgressLabel}
                                    />
                                </CancelAPICallsOnPageUnload>
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
