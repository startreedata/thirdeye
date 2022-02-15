import React, { FunctionComponent, lazy, Suspense } from "react";
import { Redirect, Route, Switch } from "react-router-dom";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import { AppRoute, getAlertsAllPath } from "../../utils/routes/routes.util";

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
            <Switch>
                {/* Alerts path */}
                <Route exact path={AppRoute.ALERTS}>
                    {/* Redirect to alerts all path */}
                    <Redirect to={getAlertsAllPath()} />
                </Route>

                {/* Alerts all path */}
                <Route
                    exact
                    component={AlertsAllPage}
                    path={AppRoute.ALERTS_ALL}
                />

                {/* Alerts view path */}
                <Route
                    exact
                    component={AlertsViewPage}
                    path={AppRoute.ALERTS_VIEW}
                />

                {/* Alerts create path */}
                <Route
                    exact
                    component={AlertsCreatePage}
                    path={AppRoute.ALERTS_CREATE}
                />

                {/* Alerts update path */}
                <Route
                    exact
                    component={AlertsUpdatePage}
                    path={AppRoute.ALERTS_UPDATE}
                />

                {/* No match found, render page not found */}
                <Route component={PageNotFoundPage} />
            </Switch>
        </Suspense>
    );
};
