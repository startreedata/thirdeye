import React, { FunctionComponent, lazy, Suspense } from "react";
import { Redirect, Route, Switch } from "react-router-dom";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { AppRoute, getMetricsAllPath } from "../../utils/routes/routes.util";

const MetricsAllPage = lazy(() =>
    import(
        /* webpackChunkName: "metrics-all-page" */ "../../pages/metrics-all-page/metrics-all-page.component"
    ).then((module) => ({ default: module.MetricsAllPage }))
);

const MetricsDetailsPage = lazy(() =>
    import(
        /* webpackChunkName: "metrics-detail-page" */ "../../pages/metrics-detail-page/metrics-details-page.component"
    ).then((module) => ({ default: module.MetricsDetailsPage }))
);

const MetricsUpdatePage = lazy(() =>
    import(
        /* webpackChunkName: "metrics-update-page" */ "../../pages/metrics-update-page/metrics-update-page.component"
    ).then((module) => ({ default: module.MetricsUpdatePage }))
);

const PageNotFoundPage = lazy(() =>
    import(
        /* webpackChunkName: "page-not-found-page" */ "../../pages/page-not-found-page/page-not-found-page.component"
    ).then((module) => ({ default: module.PageNotFoundPage }))
);

export const MetricsRouter: FunctionComponent = () => {
    return (
        <Suspense fallback={<LoadingIndicator />}>
            <Switch>
                {/* Metrics path */}
                <Route exact path={AppRoute.METRICS}>
                    {/* Redirect to metrics all path */}
                    <Redirect to={getMetricsAllPath()} />
                </Route>

                {/* Metrics all path */}
                <Route
                    exact
                    component={MetricsAllPage}
                    path={AppRoute.METRICS_ALL}
                />

                {/* Metrics details path */}
                <Route
                    exact
                    component={MetricsDetailsPage}
                    path={AppRoute.METRICS_DETAIL}
                />

                {/* Metrics update path */}
                <Route
                    exact
                    component={MetricsUpdatePage}
                    path={AppRoute.METRICS_UPDATE}
                />

                {/* No match found, render page not found */}
                <Route component={PageNotFoundPage} />
            </Switch>
        </Suspense>
    );
};
