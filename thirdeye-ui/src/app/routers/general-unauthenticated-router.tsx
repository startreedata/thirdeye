import React, { FunctionComponent, useEffect, useState } from "react";
import { Redirect, Route, Switch } from "react-router-dom";
import { PageContainer } from "../components/page-container/page-container.component";
import { PageLoadingIndicator } from "../components/page-loading-indicator/page-loading-indicator.component";
import { SignInPage } from "../pages/sign-in-page/sign-in-page.component";
import { useApplicationBreadcrumbsStore } from "../store/application-breadcrumbs/application-breadcrumbs-store";
import { Breadcrumb } from "../store/application-breadcrumbs/application-breadcrumbs-store.interfaces";
import { ApplicationRoute, getSignInPath } from "../utils/route/routes-util";

export const GeneralUnauthenticatedRouter: FunctionComponent = () => {
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
            {/* Sign in path */}
            <Route
                exact
                component={SignInPage}
                path={ApplicationRoute.SIGN_IN}
            />

            {/* No match found, redirect to sign in path*/}
            <Redirect to={getSignInPath()} />
        </Switch>
    );
};
