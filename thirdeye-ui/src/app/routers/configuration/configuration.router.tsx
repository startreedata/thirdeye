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
import { default as React, FunctionComponent, lazy, Suspense } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { CancelAPICallsOnPageUnload } from "../../components/cancel-api-calls-on-page-unload/cancel-api-calls-on-page-unload.component";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import { AppRouteRelative } from "../../utils/routes/routes.util";

const SubscriptionGroupsRouter = lazy(() =>
    import(
        /* webpackChunkName: "subscription-groups-router" */ "../subscription-groups/subscription-groups.router"
    ).then((module) => ({ default: module.SubscriptionGroupsRouter }))
);

const DatasetsRouter = lazy(() =>
    import(
        /* webpackChunkName: "datasets-router" */ "../datasets/datasets.router"
    ).then((module) => ({ default: module.DatasetsRouter }))
);

const DatasourcesRouter = lazy(() =>
    import(
        /* webpackChunkName: "datasources-router" */ "../datasources/datasources.router"
    ).then((module) => ({ default: module.DatasourcesRouter }))
);

const MetricsRouter = lazy(() =>
    import(
        /* webpackChunkName: "metrics-router" */ "../metrics/metrics.router"
    ).then((module) => ({ default: module.MetricsRouter }))
);

const AlertTemplatesRouter = lazy(() =>
    import(
        /* webpackChunkName: "metrics-router" */ "../alert-templates/alert-templates.router"
    ).then((module) => ({ default: module.AlertTemplatesRouter }))
);

const EventsRouter = lazy(() =>
    import(
        /* webpackChunkName: "metrics-router" */ "../event/event.router"
    ).then((module) => ({ default: module.EventRouter }))
);

const PageNotFoundPage = lazy(() =>
    import(
        /* webpackChunkName: "page-not-found-page" */ "../../pages/page-not-found-page/page-not-found-page.component"
    ).then((module) => ({ default: module.PageNotFoundPage }))
);

export const ConfigurationRouter: FunctionComponent = () => {
    return (
        <Suspense fallback={<AppLoadingIndicatorV1 />}>
            <Routes>
                {/* Datasources path */}
                <Route
                    index
                    element={
                        <Navigate replace to={AppRouteRelative.DATASOURCES} />
                    }
                />

                {/* Direct all subscription groups paths to subscription groups router */}
                <Route
                    element={
                        <CancelAPICallsOnPageUnload
                            key={`${AppRouteRelative.SUBSCRIPTION_GROUPS}/*`}
                        >
                            <SubscriptionGroupsRouter />
                        </CancelAPICallsOnPageUnload>
                    }
                    path={`${AppRouteRelative.SUBSCRIPTION_GROUPS}/*`}
                />

                {/* Direct all datasets paths to datasets router */}
                <Route
                    element={
                        <CancelAPICallsOnPageUnload
                            key={`${AppRouteRelative.DATASETS}/*`}
                        >
                            <DatasetsRouter />
                        </CancelAPICallsOnPageUnload>
                    }
                    path={`${AppRouteRelative.DATASETS}/*`}
                />

                {/* Direct all datasource paths to datasources router */}
                <Route
                    element={
                        <CancelAPICallsOnPageUnload
                            key={`${AppRouteRelative.DATASOURCES}/*`}
                        >
                            <DatasourcesRouter />
                        </CancelAPICallsOnPageUnload>
                    }
                    path={`${AppRouteRelative.DATASOURCES}/*`}
                />

                {/* Direct all metrics paths to metrics router */}
                <Route
                    element={
                        <CancelAPICallsOnPageUnload
                            key={`${AppRouteRelative.METRICS}/*`}
                        >
                            <MetricsRouter />
                        </CancelAPICallsOnPageUnload>
                    }
                    path={`${AppRouteRelative.METRICS}/*`}
                />

                {/* Alert templates path */}
                <Route
                    element={
                        <CancelAPICallsOnPageUnload
                            key={`${AppRouteRelative.ALERT_TEMPLATES}/*`}
                        >
                            <AlertTemplatesRouter />
                        </CancelAPICallsOnPageUnload>
                    }
                    path={`${AppRouteRelative.ALERT_TEMPLATES}/*`}
                />

                <Route
                    element={
                        <CancelAPICallsOnPageUnload
                            key={`${AppRouteRelative.EVENTS}/*`}
                        >
                            <EventsRouter />
                        </CancelAPICallsOnPageUnload>
                    }
                    path={`${AppRouteRelative.EVENTS}/*`}
                />

                {/* No match found, render page not found */}
                <Route element={<PageNotFoundPage />} path="*" />
            </Routes>
        </Suspense>
    );
};
