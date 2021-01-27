import React, { FunctionComponent, lazy, Suspense } from "react";
import { Route, Switch } from "react-router-dom";
import { useAuth } from "../../components/auth-provider/auth-provider.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { AppRoute } from "../../utils/routes-util/routes-util";

const AlertsRouter = lazy(() =>
    import(
        /* webpackChunkName: 'AlertsRouter' */
        "../alerts-router/alerts-router"
    ).then(({ AlertsRouter }) => ({
        default: AlertsRouter,
    }))
);

const AnomaliesRouter = lazy(() =>
    import(
        /* webpackChunkName: 'AnomaliesRouter' */
        "../anomalies-router/anomalies-router"
    ).then(({ AnomaliesRouter }) => ({
        default: AnomaliesRouter,
    }))
);
const ConfigurationRouter = lazy(() =>
    import(
        /* webpackChunkName: 'ConfigurationRouter' */
        "../configuration-router/configuration-router"
    ).then(({ ConfigurationRouter }) => ({
        default: ConfigurationRouter,
    }))
);
const GeneralAuthenticatedRouter = lazy(() =>
    import(
        /* webpackChunkName: 'GeneralAuthenticatedRouter' */
        "../general-authenticated-router/general-authenticated-router"
    ).then(({ GeneralAuthenticatedRouter }) => ({
        default: GeneralAuthenticatedRouter,
    }))
);
const GeneralUnauthenticatedRouter = lazy(() =>
    import(
        /* webpackChunkName: 'GeneralUnauthenticatedRouter' */
        "../general-unauthenticated-router/general-unauthenticated-router"
    ).then(({ GeneralUnauthenticatedRouter }) => ({
        default: GeneralUnauthenticatedRouter,
    }))
);

export const AppRouter: FunctionComponent = () => {
    const { authDisabled, authenticated } = useAuth();

    if (authDisabled || authenticated) {
        return (
            <Suspense fallback={<LoadingIndicator />}>
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

                    {/* Direct all other paths to general authenticated router */}
                    <Route component={GeneralAuthenticatedRouter} />
                </Switch>
            </Suspense>
        );
    }

    return (
        <Suspense fallback={<LoadingIndicator />}>
            {/* Not authenticated, direct all paths to general unauthenticated router */}
            <GeneralUnauthenticatedRouter />
        </Suspense>
    );
};
