import React, {
    FunctionComponent,
    lazy,
    Suspense,
    useEffect,
    useState,
} from "react";
import { Redirect, Route, Switch } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { AppRoute, getSignInPath } from "../../utils/routes/routes.util";

const SignInPage = lazy(() =>
    import(
        /* webpackChunkName: "sign-in-page" */ "../../pages/sign-in-page/sign-in-page.component"
    ).then((module) => ({ default: module.SignInPage }))
);

export const GeneralUnauthenticatedRouter: FunctionComponent = () => {
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
                {/* Sign in path */}
                <Route exact component={SignInPage} path={AppRoute.SIGN_IN} />

                {/* No match found, redirect to sign in path */}
                <Redirect to={getSignInPath()} />
            </Switch>
        </Suspense>
    );
};
