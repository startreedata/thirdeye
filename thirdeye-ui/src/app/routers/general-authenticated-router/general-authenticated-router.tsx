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
import { PageContainer } from "../../components/page-container/page-container.component";
import { useAppToolbarStore } from "../../store/app-toolbar-store/app-toolbar-store";
import {
    AppRoute,
    getBasePath,
    getHomePath,
} from "../../utils/routes-util/routes-util";

const HomePage = lazy(() =>
    import(
        /* webpackChunkName: 'HomePage' */
        "../../pages/home-page/home-page.component"
    ).then(({ HomePage }) => ({
        default: HomePage,
    }))
);

const SignOutPage = lazy(() =>
    import(
        /* webpackChunkName: 'SignOutPage' */
        "../../pages/sign-out-page/sign-out-page.component"
    ).then(({ SignOutPage }) => ({
        default: SignOutPage,
    }))
);

const PageNotFoundPage = lazy(() =>
    import(
        /* webpackChunkName: 'PageNotFoundPage' */
        "../../pages/page-not-found-page/page-not-found-page.component"
    ).then(({ PageNotFoundPage }) => ({
        default: PageNotFoundPage,
    }))
);

export const GeneralAuthenticatedRouter: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const { setRouterBreadcrumbs } = useAppBreadcrumbs();
    const [removeAppToolbar] = useAppToolbarStore((state) => [
        state.removeAppToolbar,
    ]);

    useEffect(() => {
        // Create router breadcrumbs
        setRouterBreadcrumbs([]);

        // No app toolbar under this router
        removeAppToolbar();

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
