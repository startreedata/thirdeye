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
import React, { FunctionComponent, lazy, Suspense, useEffect } from "react";
import { Route, Routes } from "react-router-dom";
import { CancelAPICallsOnPageUnload } from "../../components/cancel-api-calls-on-page-unload/cancel-api-calls-on-page-unload.component";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import { AppRouteRelative } from "../../utils/routes/routes.util";
import {
    SessionStorageKeys,
    useSessionStorage,
} from "../../utils/storage/use-session-storage";
import { AlertsGuidedCreateRouterProps } from "./alerts-guided-create.router.interface";

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

const SelectAlertCategoryPage = lazy(() =>
    import(
        /* webpackChunkName: "select-alert-category" */ "../../pages/alerts-create-guided-page/select-alert-category/select-alert-category-page.component"
    ).then((module) => ({ default: module.SelectAlertCategoryPage }))
);

const SelectSampleAlertPage = lazy(() =>
    import(
        /* webpackChunkName: "select-sample-alert-page" */ "../../pages/alerts-create-guided-page/select-sample-alert/select-sample-alert-page.component"
    ).then((module) => ({ default: module.SelectSampleAlertPage }))
);

const SetupMetricPage = lazy(() =>
    import(
        /* webpackChunkName: "select-metric" */ "../../pages/alerts-create-guided-page/setup-metric/setup-metric-page.component"
    ).then((module) => ({ default: module.SetupMetricPage }))
);

const SelectTypePage = lazy(() =>
    import(
        /* webpackChunkName: "select-type-page" */ "../../pages/alerts-create-guided-page/select-type/select-type-page.component"
    ).then((module) => ({ default: module.SelectTypePage }))
);

const TuneAlertPage = lazy(() =>
    import(
        /* webpackChunkName: "tune-alert-page" */ "../../pages/alerts-create-guided-page/tune-alert/tune-alert-page.component"
    ).then((module) => ({ default: module.TuneAlertPage }))
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
        createLabel,
        inProgressLabel,
        hideBottomBarForAlertCategorySelection,
    }) => {
        const [, , clearSelectedDimensions] = useSessionStorage<string[]>(
            SessionStorageKeys.SelectedDimensionsOnAlertFlow,
            []
        );
        const [, , clearQueryFilter] = useSessionStorage<string>(
            SessionStorageKeys.QueryFilterOnAlertFlow,
            ""
        );

        // Guided alert flow sets the selected dimensions and applied query filter as cache.
        // Clear those cached values once the user leaves / enters the flow from outside
        useEffect(() => {
            // Clear on flow entry as well since the session is persisted even if the user
            // manually enters a new url, in which case the value would still persist.
            clearSelectedDimensions();
            clearQueryFilter();

            return () => {
                // Clear the data on leaving the flow.
                clearSelectedDimensions();
                clearQueryFilter();
            };
        }, []);

        return (
            <Suspense fallback={<AppLoadingIndicatorV1 />}>
                <Routes>
                    <Route element={<CreateAlertGuidedPage />} path="*">
                        <Route
                            index
                            element={
                                <SelectAlertCategoryPage
                                    hideBottomBar={
                                        hideBottomBarForAlertCategorySelection
                                    }
                                />
                            }
                        />
                        <Route
                            element={<AlertsCreateEasyPage />}
                            path={AppRouteRelative.ALERTS_CREATE_EASY_ALERT}
                        />
                        <Route
                            element={<SetupMetricPage />}
                            path={
                                AppRouteRelative.WELCOME_CREATE_ALERT_SELECT_METRIC
                            }
                        />

                        <Route
                            element={<SelectSampleAlertPage />}
                            path={
                                AppRouteRelative.WELCOME_CREATE_ALERT_SAMPLE_ALERT
                            }
                        />

                        <Route
                            element={<SelectTypePage />}
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
                                        AppRouteRelative.WELCOME_CREATE_ALERT_TUNE_ALERT
                                    }
                                >
                                    <TuneAlertPage />
                                </CancelAPICallsOnPageUnload>
                            }
                            path={
                                AppRouteRelative.WELCOME_CREATE_ALERT_TUNE_ALERT
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
