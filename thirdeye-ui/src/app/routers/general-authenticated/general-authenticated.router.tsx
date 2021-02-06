import React, {
    FunctionComponent,
    lazy,
    Suspense,
    useEffect,
    useState,
} from "react";
import { Redirect, Route, Switch } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import {
    AppRoute,
    getBasePath,
    getHomePath,
} from "../../utils/routes/routes.util";

const HomePage = lazy(() =>
    import(
        /* webpackChunkName: "home-page" */ "../../pages/home-page/home-page.component"
    ).then((module) => ({ default: module.HomePage }))
);

const SignOutPage = lazy(() =>
    import(
        /* webpackChunkName: "sign-out-page" */ "../../pages/sign-out-page/sign-out-page.component"
    ).then((module) => ({ default: module.SignOutPage }))
);

const PageNotFoundPage = lazy(() =>
    import(
        /* webpackChunkName: "page-not-found-page" */ "../../pages/page-not-found-page/page-not-found-page.component"
    ).then((module) => ({ default: module.PageNotFoundPage }))
);

export const GeneralAuthenticatedRouter: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const { setRouterBreadcrumbs } = useAppBreadcrumbs();

    useEffect(() => {
        setRouterBreadcrumbs([]);
        setLoading(false);
    }, []);

    if (loading) {
        return <LoadingIndicator />;
    }

    return (
        <Suspense fallback={<LoadingIndicator />}>
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
        </Suspense>
    );
};
