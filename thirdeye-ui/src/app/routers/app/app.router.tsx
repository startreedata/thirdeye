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
import { Route, Routes } from "react-router-dom";
import {
    AppLoadingIndicatorV1,
    useAuthProviderV1,
} from "../../platform/components";
import { AppRoute } from "../../utils/routes/routes.util";

const AlertsRouter = lazy(() =>
    import(
        /* webpackChunkName: "alerts-router" */ "../alerts/alerts.router"
    ).then((module) => ({ default: module.AlertsRouter }))
);

const AnomaliesRouter = lazy(() =>
    import(
        /* webpackChunkName: "anomalies-router" */ "../anomalies/anomalies.router"
    ).then((module) => ({ default: module.AnomaliesRouter }))
);

const ConfigurationRouter = lazy(() =>
    import(
        /* webpackChunkName: "configuration-router" */ "../configuration/configuration.router"
    ).then((module) => ({ default: module.ConfigurationRouter }))
);

const GeneralAuthenticatedRouter = lazy(() =>
    import(
        /* webpackChunkName: "general-authenticated-router" */ "../general-authenticated/general-authenticated.router"
    ).then((module) => ({ default: module.GeneralAuthenticatedRouter }))
);

const GeneralUnauthenticatedRouter = lazy(() =>
    import(
        /* webpackChunkName: "general-unauthenticated-router" */ "../general-unauthenticated/general-unauthenticated.router"
    ).then((module) => ({ default: module.GeneralUnauthenticatedRouter }))
);

const RootCauseAnalysisRouter = lazy(() =>
    import(
        /* webpackChunkName: "root-cause-analysis-router" */ "../root-cause-analysis/rca.router"
    ).then((module) => ({ default: module.RootCauseAnalysisRouter }))
);

const WelcomeRouter = lazy(() =>
    import(
        /* webpackChunkName: "welcome-router" */ "../welcome/welcome.router"
    ).then((module) => ({ default: module.WelcomeRouter }))
);

export const AppRouter: FunctionComponent = () => {
    const { authDisabled, authenticated } = useAuthProviderV1();

    if (authDisabled || authenticated) {
        return (
            <Suspense fallback={<AppLoadingIndicatorV1 />}>
                <Routes>
                    {/* Direct all alerts paths to alerts router */}
                    <Route
                        element={<AlertsRouter />}
                        path={`${AppRoute.ALERTS}/*`}
                    />

                    {/* Direct all anomalies paths to anomalies router */}
                    <Route
                        element={<AnomaliesRouter />}
                        path={`${AppRoute.ANOMALIES}/*`}
                    />

                    {/* Direct all configuration paths to configuration router */}
                    <Route
                        element={<ConfigurationRouter />}
                        path={`${AppRoute.CONFIGURATION}/*`}
                    />

                    {/* Direct all rca paths root cause analysis router */}
                    <Route
                        element={<RootCauseAnalysisRouter />}
                        path={`${AppRoute.ROOT_CAUSE_ANALYSIS}/*`}
                    />

                    {/* Direct all welcome paths welcome router */}
                    <Route
                        element={<WelcomeRouter />}
                        path={`${AppRoute.WELCOME}/*`}
                    />

                    {/* Direct all other paths to general authenticated router */}
                    <Route element={<GeneralAuthenticatedRouter />} path="/*" />
                </Routes>
            </Suspense>
        );
    }

    return (
        <Suspense fallback={<AppLoadingIndicatorV1 />}>
            {/* Not authenticated, direct all paths to general unauthenticated router */}
            <GeneralUnauthenticatedRouter />
        </Suspense>
    );
};
