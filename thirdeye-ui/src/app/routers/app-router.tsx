import React, { FunctionComponent, useEffect, useState } from "react";
import { Redirect, Route, Switch } from "react-router-dom";
import { SignInPage } from "../pages/sign-in-page/sign-in-page.component";
import { isAuthenticated } from "../utils/authentication.util";
import {
    AppRoute,
    getAlertsPath,
    getBasePath,
    getSignInPath,
} from "../utils/routes.util";
import { AlertsRouter } from "./alerts-router";

// ThirdEye UI app router
export const AppRouter: FunctionComponent = () => {
    const [authenticated, setAuthenticated] = useState(false);

    useEffect(() => {
        // Determine authentication
        setAuthenticated(isAuthenticated());
    }, []);

    return (
        <Switch>
            {/* Base path */}
            <Route exact path={AppRoute.BASE}>
                {(authenticated && (
                    // Authenticated, redirect to alerts path
                    <Redirect to={getAlertsPath()} />
                )) || (
                    // Not authenticated, redirect to sign in path
                    <Redirect to={getSignInPath()} />
                )}
            </Route>

            {/* Sign in path */}
            <Route exact path={AppRoute.SIGN_IN}>
                {(authenticated && (
                    // Authenticated, redirect to base path
                    <Redirect to={getBasePath()} />
                )) || (
                    // Not authenticated, render sign in page
                    <SignInPage />
                )}
            </Route>

            {(authenticated && (
                <Switch>
                    {/* Direct all clusters paths to clusters router */}
                    <Route component={AlertsRouter} path={AppRoute.ALERTS} />
                </Switch>
            )) || (
                // Not authenticated
                <Redirect to={getSignInPath()} />
            )}
        </Switch>
    );
};
