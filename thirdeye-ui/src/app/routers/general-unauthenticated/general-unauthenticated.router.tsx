import React, { FunctionComponent, lazy, Suspense } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import { AppRoute } from "../../utils/routes/routes.util";

const LoginPage = lazy(() =>
    import(
        /* webpackChunkName: "login-page" */ "../../pages/login-page/login-page.component"
    ).then((module) => ({ default: module.LoginPage }))
);

export const GeneralUnauthenticatedRouter: FunctionComponent = () => {
    return (
        <Suspense fallback={<AppLoadingIndicatorV1 />}>
            <Routes>
                {/* Login path */}
                <Route element={<LoginPage />} path={AppRoute.LOGIN} />

                {/* No match found, redirect to login path */}
                <Route
                    element={<Navigate replace to={AppRoute.LOGIN} />}
                    path="*"
                />
            </Routes>
        </Suspense>
    );
};
