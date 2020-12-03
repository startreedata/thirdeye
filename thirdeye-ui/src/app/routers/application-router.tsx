import React, { FunctionComponent } from "react";
import { Route, Switch } from "react-router-dom";
import { useAuthStore } from "../store/auth/auth-store";
import { ApplicationRoute } from "../utils/routes/routes-util";
import { AlertsRouter } from "./alerts-router";
import { AnomaliesRouter } from "./anomalies-router";
import { GeneralAuthenticatedRouter } from "./general-authenticated-router";
import { GeneralUnauthenticatedRouter } from "./general-unauthenticated-router";

export const ApplicationRouter: FunctionComponent = () => {
    const [auth] = useAuthStore((state) => [state.auth]);

    if (auth) {
        return (
            // Authenticated
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
        // Not authenticated
        <Switch>
            {/* Direct all paths to general unauthenticated router */}
            <Route component={GeneralUnauthenticatedRouter} />
        </Switch>
    );
};
