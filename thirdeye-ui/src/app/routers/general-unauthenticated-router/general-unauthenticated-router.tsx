import React, { FunctionComponent, useEffect } from "react";
import { Redirect, Route, Switch, useLocation } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { SignInPage } from "../../pages/sign-in-page/sign-in-page.component";
import { useAppToolbarStore } from "../../store/app-toolbar-store/app-toolbar-store";
import { useRedirectionPathStore } from "../../store/redirection-path-store/redirection-path-store";
import {
    AppRoute,
    createPathWithRecognizedQueryString,
    getSignInPath,
} from "../../utils/routes-util/routes-util";

export const GeneralUnauthenticatedRouter: FunctionComponent = () => {
    const { setRouterBreadcrumbs } = useAppBreadcrumbs();
    const [removeAppToolbar] = useAppToolbarStore((state) => [
        state.removeAppToolbar,
    ]);
    const [setRedirectionPath] = useRedirectionPathStore((state) => [
        state.setRedirectionPath,
    ]);
    const location = useLocation();

    useEffect(() => {
        // Create router breadcrumbs
        setRouterBreadcrumbs([]);

        // No app toolbar under this router
        removeAppToolbar();

        setupRedirectionPath();
    }, []);

    const setupRedirectionPath = (): void => {
        // If location is anything other than the sign in/out path, store it to redirect the user
        // after authentication
        if (
            location.pathname === AppRoute.SIGN_IN ||
            location.pathname === AppRoute.SIGN_OUT
        ) {
            return;
        }

        setRedirectionPath(
            createPathWithRecognizedQueryString(location.pathname)
        );
    };

    return (
        <Switch>
            {/* Sign in path */}
            <Route exact component={SignInPage} path={AppRoute.SIGN_IN} />

            {/* No match found, redirect to sign in path */}
            <Redirect to={getSignInPath()} />
        </Switch>
    );
};
