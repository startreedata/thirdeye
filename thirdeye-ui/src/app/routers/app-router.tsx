import React, { FunctionComponent, useEffect, useState } from "react";
import { Redirect, Route, Switch } from "react-router-dom";
import { PageContainer } from "../components/page-container/page-container.component";
import { PageLoadingIndicator } from "../components/page-loading-indicator/page-loading-indicator.component";
import { PageNotFoundPage } from "../pages/page-not-found-page/page-not-found-page.component";
import { SignInPage } from "../pages/sign-in-page/sign-in-page.component";
import { SignOutPage } from "../pages/sign-out-page/sign-out-page.component";
import { isAuthenticated } from "../utils/auth/auth.util";
import {
    AppRoute,
    getBasePath,
    getHomePath,
    getPageNotFoundPath,
    getSignInPath,
} from "../utils/route/routes.util";
import { AlertsRouter } from "./alerts-router";
import { AnomaliesRouter } from "./anomalies-router";
import { HomeRouter } from "./home-router";

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
                {/* Base path */}
                <Route exact path={AppRoute.BASE}>
                    {/* Redirect to home path */}
                    <Redirect to={getHomePath()} />
                </Route>

                {/* Direct all home paths to home router */}
                <Route component={HomeRouter} path={AppRoute.HOME} />

                {/* Direct all alerts paths to alerts router */}
                <Route component={AlertsRouter} path={AppRoute.ALERTS} />

                {/* Direct all anomalies paths to anomalies router */}
                <Route component={AnomaliesRouter} path={AppRoute.ANOMALIES} />

                {/* /Sign in path */}
                <Route exact path={AppRoute.SIGN_IN}>
                    {/* Already authenticated, redirect to base path */}
                    <Redirect to={getBasePath()} />
                </Route>

                {/* Sign out path */}
                <Route exact component={SignOutPage} path={AppRoute.SIGN_OUT} />

                {/* Page not found path */}
                <Route
                    exact
                    component={PageNotFoundPage}
                    path={AppRoute.PAGE_NOT_FOUND}
                />

                {/* No match found, redirect to page not found path */}
                <Redirect to={getPageNotFoundPath()} />
            </Switch>
        );
    }

    return (
        // Not authenticated
        <Switch>
            {/* Sign in path */}
            <Route exact component={SignInPage} path={AppRoute.SIGN_IN} />

            {/* No match found, redirect to sign in path*/}
            <Redirect to={getSignInPath()} />
        </Switch>
    );
};
