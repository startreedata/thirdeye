import React, { FunctionComponent, useEffect, useState } from "react";
import { Redirect, Route, Switch } from "react-router-dom";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { HomePage } from "../../pages/home-page/home-page.component";
import { PageNotFoundPage } from "../../pages/page-not-found-page/page-not-found-page.component";
import { SignOutPage } from "../../pages/sign-out-page/sign-out-page.component";
import { useAppBreadcrumbsStore } from "../../store/app-breadcrumbs-store/app-breadcrumbs-store";
import {
    AppRoute,
    getBasePath,
    getHomePath,
} from "../../utils/routes-util/routes-util";

export const GeneralAuthenticatedRouter: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [setAppSectionBreadcrumbs] = useAppBreadcrumbsStore((state) => [
        state.setAppSectionBreadcrumbs,
    ]);

    useEffect(() => {
        // Create app section breadcrumbs
        setAppSectionBreadcrumbs([]);

        setLoading(false);
    }, []);

    if (loading) {
        return (
            <PageContainer>
                <LoadingIndicator />
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
