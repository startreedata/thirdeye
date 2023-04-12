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
import { AppLoadingIndicatorV1 } from "../../platform/components";
import { AppRouteRelative } from "../../utils/routes/routes.util";
import { SubscriptionGroupsCreateEditRouterProps } from "./subscription-groups-create-edit.router.interface";

const SubscriptionGroupsWizardDetailsPage = lazy(() =>
    import(
        /* webpackChunkName: "setup-details-page" */ "../../pages/subscription-groups-wizard-page/setup-details/setup-details-page.component"
    ).then((module) => ({ default: module.SetupDetailsPage }))
);

const SubscriptionGroupsWizardAlertDimensionsPage = lazy(() =>
    import(
        /* webpackChunkName: "setup-alert-dimensions-page" */ "../../pages/subscription-groups-wizard-page/setup-alert-dimensions/setup-alert-dimensions-page.component"
    ).then((module) => ({ default: module.SetupAlertDimensionsPage }))
);

const PageNotFoundPage = lazy(() =>
    import(
        /* webpackChunkName: "page-not-found-page" */ "../../pages/page-not-found-page/page-not-found-page.component"
    ).then((module) => ({ default: module.PageNotFoundPage }))
);

export const SubscriptionGroupsCreateEditRouter: FunctionComponent<SubscriptionGroupsCreateEditRouterProps> =
    ({ containerPage }) => {
        return (
            <Suspense fallback={<AppLoadingIndicatorV1 />}>
                <Routes>
                    {/* Subscription groups create path */}
                    <Route element={containerPage} path="*">
                        <Route
                            index
                            element={
                                <Navigate
                                    replace
                                    to={
                                        AppRouteRelative.SUBSCRIPTION_GROUPS_WIZARD_DETAILS
                                    }
                                />
                            }
                        />
                        <Route
                            element={<SubscriptionGroupsWizardDetailsPage />}
                            path={
                                AppRouteRelative.SUBSCRIPTION_GROUPS_WIZARD_DETAILS
                            }
                        />
                        <Route
                            element={
                                <SubscriptionGroupsWizardAlertDimensionsPage />
                            }
                            path={
                                AppRouteRelative.SUBSCRIPTION_GROUPS_WIZARD_ALERT_DIMENSIONS
                            }
                        />
                    </Route>

                    {/* No match found, render page not found */}
                    <Route element={<PageNotFoundPage />} path="*" />
                </Routes>
            </Suspense>
        );
    };
