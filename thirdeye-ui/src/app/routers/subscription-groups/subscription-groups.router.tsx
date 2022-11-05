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
import React, { FunctionComponent, lazy, Suspense } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import {
    AppRouteRelative,
    getSubscriptionGroupsAllPath,
} from "../../utils/routes/routes.util";

const SubscriptionGroupsAllPage = lazy(() =>
    import(
        /* webpackChunkName: "subscription-groups-all-page" */ "../../pages/subscription-groups-all-page/subscription-groups-all-page.component"
    ).then((module) => ({ default: module.SubscriptionGroupsAllPage }))
);

const SubscriptionGroupsViewPage = lazy(() =>
    import(
        /* webpackChunkName: "subscription-groups-view-page" */ "../../pages/subscription-groups-view-page/subscription-groups-view-page.component"
    ).then((module) => ({ default: module.SubscriptionGroupsViewPage }))
);

const SubscriptionGroupsCreatePage = lazy(() =>
    import(
        /* webpackChunkName: "subscription-groups-create-page" */ "../../pages/subscription-groups-create-page/subscription-groups-create-page.component"
    ).then((module) => ({ default: module.SubscriptionGroupsCreatePage }))
);

const SubscriptionGroupsUpdatePage = lazy(() =>
    import(
        /* webpackChunkName: "subscription-groups-update-page" */ "../../pages/subscription-groups-update-page/subscription-groups-update-page.component"
    ).then((module) => ({ default: module.SubscriptionGroupsUpdatePage }))
);

const PageNotFoundPage = lazy(() =>
    import(
        /* webpackChunkName: "page-not-found-page" */ "../../pages/page-not-found-page/page-not-found-page.component"
    ).then((module) => ({ default: module.PageNotFoundPage }))
);

export const SubscriptionGroupsRouter: FunctionComponent = () => {
    return (
        <Suspense fallback={<AppLoadingIndicatorV1 />}>
            <Routes>
                {/* Subscription groups path */}
                {/* Redirect to subscription groups all path */}
                <Route
                    index
                    element={
                        <Navigate replace to={getSubscriptionGroupsAllPath()} />
                    }
                />

                {/* Subscription groups all path */}
                <Route
                    element={<SubscriptionGroupsAllPage />}
                    path={AppRouteRelative.SUBSCRIPTION_GROUPS_ALL}
                />

                {/* Subscription groups view path */}
                <Route
                    element={<SubscriptionGroupsViewPage />}
                    path={AppRouteRelative.SUBSCRIPTION_GROUPS_VIEW}
                />

                {/* Subscription groups create path */}
                <Route
                    element={<SubscriptionGroupsCreatePage />}
                    path={AppRouteRelative.SUBSCRIPTION_GROUPS_CREATE}
                />

                {/* Subscription groups update path */}
                <Route
                    element={<SubscriptionGroupsUpdatePage />}
                    path={AppRouteRelative.SUBSCRIPTION_GROUPS_UPDATE}
                />

                {/* No match found, render page not found */}
                <Route element={<PageNotFoundPage />} path="*" />
            </Routes>
        </Suspense>
    );
};
