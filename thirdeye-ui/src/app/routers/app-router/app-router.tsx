import React, { FunctionComponent } from "react";
import { Route, Switch } from "react-router-dom";
import { useAuthStore } from "../../store/auth-store/auth-store";
import { ApplicationRoute } from "../../utils/routes-util/routes-util";
import { AlertsRouter } from "../alerts-router/alerts-router";
import { AnomaliesRouter } from "../anomalies-router/anomalies-router";
import { GeneralAuthenticatedRouter } from "../general-authenticated-router/general-authenticated-router";
import { GeneralUnauthenticatedRouter } from "../general-unauthenticated-router/general-unauthenticated-router";

export const AppRouter: FunctionComponent = () => {
    const [auth] = useAuthStore((state) => [state.auth]);

    if (auth) {
        // Authenticated
        return (
            <Switch>
                {/* Direct all alerts paths to alerts router */}
                <Route
                    component={AlertsRouter}
                    path={ApplicationRoute.ALERTS}
                />

                {/* Direct all anomalies paths to anomalies router */}
                <Route
                    component={AnomaliesRouter}
                    path={ApplicationRoute.ANOMALIES}
                />

                {/* Direct all other paths to general authenticated router */}
                <Route component={GeneralAuthenticatedRouter} />
            </Switch>
        );
    }

    return (
        // Not authenticated, direct all paths to general unauthenticated router
        <GeneralUnauthenticatedRouter />
    );
};
