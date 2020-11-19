import React, { FunctionComponent, useEffect, useState } from "react";
import { Redirect, Route, Switch } from "react-router-dom";
import { PageContainer } from "../components/page-container/page-container.component";
import { PageLoadingIndicator } from "../components/page-loading-indicator/page-loading-indicator.component";
import { HomePage } from "../pages/home/home-page.component";
import { PageNotFoundPage } from "../pages/page-not-found/page-not-found-page.component";
import { SignOutPage } from "../pages/sign-out/sign-out-page.component";
import { useApplicationBreadcrumbsStore } from "../store/application-breadcrumbs/application-breadcrumbs-store";
import { Breadcrumb } from "../store/application-breadcrumbs/application-breadcrumbs-store.interfaces";
import {
    ApplicationRoute,
    getBasePath,
    getHomePath,
} from "../utils/route/routes-util";

export const GeneralAuthenticatedRouter: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [setRouterBreadcrumb] = useApplicationBreadcrumbsStore((state) => [
        state.setRouterBreadcrumb,
    ]);

    useEffect(() => {
        // Create router breadcrumb
        setRouterBreadcrumb({} as Breadcrumb);

        setLoading(false);
    }, [setRouterBreadcrumb]);

    if (loading) {
        return (
            <PageContainer>
                <PageLoadingIndicator />
            </PageContainer>
        );
    }

    return (
        <Switch>
            {/* Base path */}
            <Route exact path={ApplicationRoute.BASE}>
                {/* Redirect to home path */}
                <Redirect to={getHomePath()} />
            </Route>

            {/* Home path */}
            <Route exact component={HomePage} path={ApplicationRoute.HOME} />

            {/* Sign in path */}
            <Route exact path={ApplicationRoute.SIGN_IN}>
                {/* Already authenticated, redirect to base path */}
                <Redirect to={getBasePath()} />
            </Route>

            {/* Sign out path */}
            <Route
                exact
                component={SignOutPage}
                path={ApplicationRoute.SIGN_OUT}
            />

            {/* No match found, render page not found */}
            <Route component={PageNotFoundPage} />
        </Switch>
    );
};
