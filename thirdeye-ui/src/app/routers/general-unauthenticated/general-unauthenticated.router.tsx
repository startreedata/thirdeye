import React, { FunctionComponent, lazy, Suspense } from "react";
import { Redirect, Route, Switch } from "react-router-dom";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import { AppRoute, getLoginPath } from "../../utils/routes/routes.util";

const LoginPage = lazy(() =>
    import(
        /* webpackChunkName: "login-page" */ "../../pages/login-page/login-page.component"
    ).then((module) => ({ default: module.LoginPage }))
);

export const GeneralUnauthenticatedRouter: FunctionComponent = () => {
    return (
        <Suspense fallback={<AppLoadingIndicatorV1 />}>
            <Switch>
                {/* Login path */}
                <Route exact component={LoginPage} path={AppRoute.LOGIN} />

                {/* No match found, redirect to login path */}
                <Redirect to={getLoginPath()} />
            </Switch>
        </Suspense>
    );
};
