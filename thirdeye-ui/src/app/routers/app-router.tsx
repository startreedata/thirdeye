import React, { FunctionComponent, useEffect, useState } from "react";
import { Route, Switch } from "react-router-dom";
import { PageContainer } from "../components/page-container/page-container.component";
import { PageLoadingIndicator } from "../components/page-loading-indicator/page-loading-indicator.component";
import { isAuthenticated } from "../utils/auth/auth-util";
import { ApplicationRoute } from "../utils/route/routes-util";
import { AlertsRouter } from "./alerts-router";
import { AnomaliesRouter } from "./anomalies-router";
import { GeneralAuthenticatedRouter } from "./general-authenticated-router";
import { GeneralUnauthenticatedRouter } from "./general-unauthenticated-router";

export const AppRouter: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [authenticated, setAuthenticated] = useState(false);

    useEffect(() => {
        // Determine authentication
        setAuthenticated(isAuthenticated());

        setLoading(false);
    }, []);

    if (loading) {
        return (
            <PageContainer>
                <PageLoadingIndicator />
            </PageContainer>
        );
    }

    if (authenticated) {
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
            {/* Direct all paths to geenral unauthenticated router */}
            <Route component={GeneralUnauthenticatedRouter} />
        </Switch>
    );
};
