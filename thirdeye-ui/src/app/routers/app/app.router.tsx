import {
    AppLoadingIndicatorV1,
    useAuthProviderV1,
} from "@startree-ui/platform-ui";
import React, { FunctionComponent, lazy, Suspense } from "react";
import { Route, Switch } from "react-router-dom";
import { AppRoute } from "../../utils/routes/routes.util";
import { RootCauseAnalysisRouter } from "../root-cause-analysis/rca.router";

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

export const AppRouter: FunctionComponent = () => {
    const { authDisabled, authenticated } = useAuthProviderV1();

    if (authDisabled || authenticated) {
        return (
            <Suspense fallback={<AppLoadingIndicatorV1 />}>
                <Switch>
                    {/* Direct all alerts paths to alerts router */}
                    <Route component={AlertsRouter} path={AppRoute.ALERTS} />

                    {/* Direct all anomalies paths to anomalies router */}
                    <Route
                        component={AnomaliesRouter}
                        path={AppRoute.ANOMALIES}
                    />

                    {/* Direct all configuration paths to configuration router */}
                    <Route
                        component={ConfigurationRouter}
                        path={AppRoute.CONFIGURATION}
                    />

                    {/* Direct all rca paths root cause analysis router */}
                    <Route
                        component={RootCauseAnalysisRouter}
                        path={AppRoute.ROOT_CAUSE_ANALYSIS}
                    />

                    {/* Direct all other paths to general authenticated router */}
                    <Route component={GeneralAuthenticatedRouter} />
                </Switch>
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
