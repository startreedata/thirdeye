import React, { FunctionComponent, useEffect } from "react";
import { Redirect, Route, Switch } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { HomePage } from "../../pages/home-page/home-page.component";
import { PageNotFoundPage } from "../../pages/page-not-found-page/page-not-found-page.component";
import { SignOutPage } from "../../pages/sign-out-page/sign-out-page.component";
import { useAppToolbarStore } from "../../store/app-toolbar-store/app-toolbar-store";
import {
    AppRoute,
    getBasePath,
    getHomePath,
} from "../../utils/routes-util/routes-util";

export const GeneralAuthenticatedRouter: FunctionComponent = () => {
    const { setRouterBreadcrumbs } = useAppBreadcrumbs();
    const [removeAppToolbar] = useAppToolbarStore((state) => [
        state.removeAppToolbar,
    ]);

    useEffect(() => {
        // Create router breadcrumbs
        setRouterBreadcrumbs([]);

        // No app toolbar under this router
        removeAppToolbar();
    }, []);

    return (
        <Switch>
            {/* Base path */}
            <Route exact path={AppRoute.BASE}>
                {/* Redirect to home path */}
                <Redirect to={getHomePath()} />
            </Route>

            {/* Home path */}
            <Route exact component={HomePage} path={AppRoute.HOME} />

            {/* Sign in path */}
            <Route exact path={AppRoute.SIGN_IN}>
                {/* Already authenticated, redirect to base path */}
                <Redirect to={getBasePath()} />
            </Route>

            {/* Sign out path */}
            <Route exact component={SignOutPage} path={AppRoute.SIGN_OUT} />

            {/* No match found, render page not found */}
            <Route component={PageNotFoundPage} />
        </Switch>
    );
};
