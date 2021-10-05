import { AppLoadingIndicatorV1 } from "@startree-ui/platform-ui";
import React, {
    FunctionComponent,
    lazy,
    Suspense,
    useEffect,
    useState,
} from "react";
import { Redirect, Route, Switch } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { AppRoute, getLoginPath } from "../../utils/routes/routes.util";

const LoginPage = lazy(() =>
    import(
        /* webpackChunkName: "login-page" */ "../../pages/login-page/login-page.component"
    ).then((module) => ({ default: module.LoginPage }))
);

export const GeneralUnauthenticatedRouter: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const { setRouterBreadcrumbs } = useAppBreadcrumbs();

    useEffect(() => {
        setRouterBreadcrumbs([]);
        setLoading(false);
    }, []);

    if (loading) {
        return <AppLoadingIndicatorV1 />;
    }

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
