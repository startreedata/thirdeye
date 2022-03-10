import { default as React, FunctionComponent, lazy, Suspense } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import { AppRouteRelative } from "../../utils/routes/routes.util";

const SubscriptionGroupsRouter = lazy(() =>
    import(
        /* webpackChunkName: "subscription-groups-router" */ "../subscription-groups/subscription-groups.router"
    ).then((module) => ({ default: module.SubscriptionGroupsRouter }))
);

const DatasetsRouter = lazy(() =>
    import(
        /* webpackChunkName: "datasets-router" */ "../datasets/datasets.router"
    ).then((module) => ({ default: module.DatasetsRouter }))
);

const DatasourcesRouter = lazy(() =>
    import(
        /* webpackChunkName: "datasources-router" */ "../datasources/datasources.router"
    ).then((module) => ({ default: module.DatasourcesRouter }))
);

const MetricsRouter = lazy(() =>
    import(
        /* webpackChunkName: "metrics-router" */ "../metrics/metrics.router"
    ).then((module) => ({ default: module.MetricsRouter }))
);

const AlertTemplatesRouter = lazy(() =>
    import(
        /* webpackChunkName: "metrics-router" */ "../alert-templates/alert-templates.router"
    ).then((module) => ({ default: module.AlertTemplatesRouter }))
);

const PageNotFoundPage = lazy(() =>
    import(
        /* webpackChunkName: "page-not-found-page" */ "../../pages/page-not-found-page/page-not-found-page.component"
    ).then((module) => ({ default: module.PageNotFoundPage }))
);

export const ConfigurationRouter: FunctionComponent = () => {
    return (
        <Suspense fallback={<AppLoadingIndicatorV1 />}>
            <Routes>
                {/* Configuration path */}
                <Route
                    index
                    element={
                        <Navigate replace to={AppRouteRelative.DATASOURCES} />
                    }
                />

                {/* Direct all subscription groups paths to subscription groups router */}
                <Route
                    element={<SubscriptionGroupsRouter />}
                    path={`${AppRouteRelative.SUBSCRIPTION_GROUPS}/*`}
                />

                {/* Direct all datasets paths to datasets router */}
                <Route
                    element={<DatasetsRouter />}
                    path={`${AppRouteRelative.DATASETS}/*`}
                />

                {/* Direct all datasource paths to datasources router */}
                <Route
                    element={<DatasourcesRouter />}
                    path={`${AppRouteRelative.DATASOURCES}/*`}
                />

                {/* Direct all metrics paths to metrics router */}
                <Route
                    element={<MetricsRouter />}
                    path={`${AppRouteRelative.METRICS}/*`}
                />

                {/* Alert templates path */}
                <Route
                    element={<AlertTemplatesRouter />}
                    path={`${AppRouteRelative.ALERT_TEMPLATES}/*`}
                />

                {/* No match found, render page not found */}
                <Route element={<PageNotFoundPage />} path="*" />
            </Routes>
        </Suspense>
    );
};
