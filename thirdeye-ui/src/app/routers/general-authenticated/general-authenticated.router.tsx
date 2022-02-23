import React, {
    FunctionComponent,
    lazy,
    Suspense,
    useEffect,
    useState,
} from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import {
    AppRoute,
    AppRouteRelative,
    getBasePath,
    getHomePath,
} from "../../utils/routes/routes.util";

const HomePage = lazy(() =>
    import(
        /* webpackChunkName: "home-page" */ "../../pages/home-page/home-page.component"
    ).then((module) => ({ default: module.HomePage }))
);

const LogoutPage = lazy(() =>
    import(
        /* webpackChunkName: "logout-page" */ "../../pages/logout-page/logout-page.component"
    ).then((module) => ({ default: module.LogoutPage }))
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
        return <AppLoadingIndicatorV1 />;
    }

    return (
        <Suspense fallback={<AppLoadingIndicatorV1 />}>
            <Routes>
                {/* Base path */}
                {/* Redirect to home path */}
                <Route
                    element={<Navigate replace to={getHomePath()} />}
                    path={AppRoute.BASE}
                />

                {/* Home path */}
                <Route element={<HomePage />} path={AppRouteRelative.HOME} />

                {/* Login path */}
                {/* Already authenticated, redirect to base path */}
                <Route
                    element={<Navigate replace to={getBasePath()} />}
                    path={AppRouteRelative.LOGIN}
                />

                {/* Logout path */}
                <Route
                    element={<LogoutPage />}
                    path={AppRouteRelative.LOGOUT}
                />

                {/* No match found, render page not found */}
                <Route element={<PageNotFoundPage />} path="*" />
            </Routes>
        </Suspense>
    );
};
