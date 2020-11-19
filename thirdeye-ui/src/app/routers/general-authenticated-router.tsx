import React, { FunctionComponent, useEffect, useState } from "react";
import { Redirect, Route, Switch } from "react-router-dom";
import { PageContainer } from "../components/page-container/page-container.component";
import { PageLoadingIndicator } from "../components/page-loading-indicator/page-loading-indicator.component";
import { HomePage } from "../pages/home-page/home-page.component";
import { PageNotFoundPage } from "../pages/page-not-found-page/page-not-found-page.component";
import { SignOutPage } from "../pages/sign-out-page/sign-out-page.component";
import { Breadcrumb } from "../store/application-breadcrumbs/application-breadcrumbs.interfaces";
import { useApplicationBreadcrumbsStore } from "../store/application-breadcrumbs/application-breadcrumbs.store";
import {
    AppRoute,
    getBasePath,
    getHomePath,
    getPageNotFoundPath,
} from "../utils/route/routes.util";

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
            <Route exact path={AppRoute.BASE}>
                {/* Redirect to home path */}
                <Redirect to={getHomePath()} />
            </Route>

            {/* Home path */}
            <Route exact component={HomePage} path={AppRoute.HOME} />

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
};
