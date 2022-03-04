import React, { FunctionComponent, lazy, Suspense } from "react";
import { Navigate, Outlet, Route, Routes } from "react-router-dom";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import { RedirectValidation } from "../../utils/routes/redirect-validation/redirect-validation.component";
import { RedirectWithDefaultParams } from "../../utils/routes/redirect-with-default-params/redirect-with-default-params.component";
import { AppRouteRelative } from "../../utils/routes/routes.util";

const AlertsAllPage = lazy(() =>
    import(
        /* webpackChunkName: "alerts-all-page" */ "../../pages/alerts-all-page/alerts-all-page.component"
    ).then((module) => ({ default: module.AlertsAllPage }))
);

const AlertsViewPage = lazy(() =>
    import(
        /* webpackChunkName: "alerts-view-page" */ "../../pages/alerts-view-page/alerts-view-page.component"
    ).then((module) => ({ default: module.AlertsViewPage }))
);

const AlertsCreatePage = lazy(() =>
    import(
        /* webpackChunkName: "alerts-create-page" */ "../../pages/alerts-create-page/alerts-create-page.component"
    ).then((module) => ({ default: module.AlertsCreatePage }))
);

const AlertsUpdatePage = lazy(() =>
    import(
        /* webpackChunkName: "alerts-update-page" */ "../../pages/alerts-update-page/alerts-update-page.component"
    ).then((module) => ({ default: module.AlertsUpdatePage }))
);

const PageNotFoundPage = lazy(() =>
    import(
        /* webpackChunkName: "page-not-found-page" */ "../../pages/page-not-found-page/page-not-found-page.component"
    ).then((module) => ({ default: module.PageNotFoundPage }))
);

export const AlertsRouter: FunctionComponent = () => {
    return (
        <Suspense fallback={<AppLoadingIndicatorV1 />}>
            <Routes>
                {/* Alerts path */}
                {/* Redirect to alerts all path */}
                <Route
                    index
                    element={<Navigate to={AppRouteRelative.ALERTS_ALL} />}
                />

                {/* Alerts all path */}
                <Route
                    element={<AlertsAllPage />}
                    path={AppRouteRelative.ALERTS_ALL}
                />

                {/* Alerts create path */}
                <Route
                    element={<AlertsCreatePage />}
                    path={AppRouteRelative.ALERTS_CREATE}
                />

                {/* Alert paths */}
                <Route
                    element={<Outlet />}
                    path={`${AppRouteRelative.ALERTS_ALERT}/*`}
                >
                    <Route
                        index
                        element={
                            <RedirectWithDefaultParams
                                to={AppRouteRelative.ALERTS_VIEW}
                            />
                        }
                    />

                    <Route
                        element={
                            <RedirectValidation
                                queryParams={[
                                    "timeRange",
                                    "startTime",
                                    "endTime",
                                ]}
                                to=".."
                            >
                                <AlertsViewPage />
                            </RedirectValidation>
                        }
                        path={AppRouteRelative.ALERTS_VIEW}
                    />
                    <Route
                        element={<AlertsUpdatePage />}
                        path={AppRouteRelative.ALERTS_UPDATE}
                    />

                    {/* No match found, render page not found */}
                    <Route element={<PageNotFoundPage />} path="*" />
                </Route>

                {/* No match found, render page not found */}
                <Route element={<PageNotFoundPage />} path="*" />
            </Routes>
        </Suspense>
    );
};
