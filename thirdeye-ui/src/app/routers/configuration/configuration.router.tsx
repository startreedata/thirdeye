import { AppLoadingIndicatorV1 } from "@startree-ui/platform-ui";
import React, { FunctionComponent, lazy, Suspense, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { Redirect, Route, Switch, useHistory } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { AppRoute, getConfigurationPath } from "../../utils/routes/routes.util";

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

const PageNotFoundPage = lazy(() =>
    import(
        /* webpackChunkName: "page-not-found-page" */ "../../pages/page-not-found-page/page-not-found-page.component"
    ).then((module) => ({ default: module.PageNotFoundPage }))
);

export const ConfigurationRouter: FunctionComponent = () => {
    const { setRouterBreadcrumbs } = useAppBreadcrumbs();
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        setRouterBreadcrumbs([
            {
                text: t("label.configuration"),
                onClick: () => history.push(getConfigurationPath()),
            },
        ]);
    }, []);

    return (
        <Suspense fallback={<AppLoadingIndicatorV1 />}>
            <Switch>
                {/* Configuration path */}
                <Route exact path={AppRoute.CONFIGURATION}>
                    <Redirect to={AppRoute.SUBSCRIPTION_GROUPS} />
                </Route>

                {/* Direct all subscription groups paths to subscription groups router */}
                <Route
                    component={SubscriptionGroupsRouter}
                    path={AppRoute.SUBSCRIPTION_GROUPS}
                />

                {/* Direct all datasets paths to datasets router */}
                <Route component={DatasetsRouter} path={AppRoute.DATASETS} />

                {/* Direct all datasource paths to datasources router */}
                <Route
                    component={DatasourcesRouter}
                    path={AppRoute.DATASOURCES}
                />

                {/* Direct all metrics paths to metrics router */}
                <Route component={MetricsRouter} path={AppRoute.METRICS} />

                {/* No match found, render page not found */}
                <Route component={PageNotFoundPage} />
            </Switch>
        </Suspense>
    );
};
