import React, {
    FunctionComponent,
    lazy,
    ReactNode,
    Suspense,
    useEffect,
    useState,
} from "react";
import {
    Redirect,
    Route,
    RouteComponentProps,
    Switch,
    useLocation,
} from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import {
    AppRoute,
    createPathWithRecognizedQueryString,
    getBasePath,
    getSignInPath,
} from "../../utils/routes-util/routes-util";

const SignInPage = lazy(() =>
    import(
        /* webpackChunkName: "sign-in-page" */ "../../pages/sign-in-page/sign-in-page.component"
    ).then((module) => ({ default: module.SignInPage }))
);

export const GeneralUnauthenticatedRouter: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [redirectURL, setRedirectURL] = useState(getBasePath());
    const { setRouterBreadcrumbs } = useAppBreadcrumbs();
    const location = useLocation();

    useEffect(() => {
        setRouterBreadcrumbs([]);
        initRedirectionURL();
        setLoading(false);
    }, []);

    const initRedirectionURL = (): void => {
        // If location is anything other than sign in/out path, store it to redirect the user after
        // authentication
        if (
            location.pathname === AppRoute.SIGN_IN ||
            location.pathname === AppRoute.SIGN_OUT
        ) {
            setRedirectURL(getBasePath());

            return;
        }

        setRedirectURL(createPathWithRecognizedQueryString(location.pathname));
    };

    if (loading) {
        return <LoadingIndicator />;
    }

    return (
        <Suspense fallback={<LoadingIndicator />}>
            <Switch>
                {/* Sign in path */}
                <Route
                    exact
                    path={AppRoute.SIGN_IN}
                    render={(props: RouteComponentProps): ReactNode => (
                        <SignInPage {...props} redirectURL={redirectURL} />
                    )}
                />

                {/* No match found, redirect to sign in path */}
                <Redirect to={getSignInPath()} />
            </Switch>
        </Suspense>
    );
};
