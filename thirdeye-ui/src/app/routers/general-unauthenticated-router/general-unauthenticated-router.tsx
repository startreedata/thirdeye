import React, {
    FunctionComponent,
    ReactNode,
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
import { SignInPage } from "../../pages/sign-in-page/sign-in-page.component";
import { useAppToolbarStore } from "../../store/app-toolbar-store/app-toolbar-store";
import {
    AppRoute,
    createPathWithRecognizedQueryString,
    getBasePath,
    getSignInPath,
} from "../../utils/routes-util/routes-util";

export const GeneralUnauthenticatedRouter: FunctionComponent = () => {
    const [redirectionURL, setRedirectionURL] = useState(getBasePath());
    const { setRouterBreadcrumbs } = useAppBreadcrumbs();
    const [removeAppToolbar] = useAppToolbarStore((state) => [
        state.removeAppToolbar,
    ]);
    const location = useLocation();

    useEffect(() => {
        // Create router breadcrumbs
        setRouterBreadcrumbs([]);

        // No app toolbar under this router
        removeAppToolbar();

        initRedirectionURL();
    }, []);

    const initRedirectionURL = (): void => {
        // If location is anything other than the sign in/out path, store it to redirect the user
        // after authentication
        if (
            location.pathname === AppRoute.SIGN_IN ||
            location.pathname === AppRoute.SIGN_OUT
        ) {
            setRedirectionURL(getBasePath());

            return;
        }

        setRedirectionURL(
            createPathWithRecognizedQueryString(location.pathname)
        );
    };

    return (
        <Switch>
            {/* Sign in path */}
            <Route
                exact
                path={AppRoute.SIGN_IN}
                render={(props: RouteComponentProps): ReactNode => (
                    <SignInPage {...props} redirectionURL={redirectionURL} />
                )}
            />

            {/* No match found, redirect to sign in path */}
            <Redirect to={getSignInPath()} />
        </Switch>
    );
};
