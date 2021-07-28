import React, {
    FunctionComponent,
    lazy,
    Suspense,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { Route, Switch, useHistory } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { AppRoute, getConfigurationPath } from "../../utils/routes/routes.util";

const ConfigurationPage = lazy(() =>
    import(
        /* webpackChunkName: "configuration-page" */ "../../pages/configuration-page/configuration-page.component"
    ).then((module) => ({ default: module.ConfigurationPage }))
);

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
    const [loading, setLoading] = useState(true);
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
        setLoading(false);
    }, []);

    if (loading) {
        return <LoadingIndicator />;
    }

    return (
        <Suspense fallback={<LoadingIndicator />}>
            <Switch>
                {/* Configuration path */}
                <Route
                    exact
                    component={ConfigurationPage}
                    path={AppRoute.CONFIGURATION}
                />

                {/* Direct all subscription groups paths to subscription groups router */}
                <Route
                    component={SubscriptionGroupsRouter}
                    path={AppRoute.SUBSCRIPTION_GROUPS}
                />

                {/* Direct all datasets paths to datasets router */}
                <Route component={DatasetsRouter} path={AppRoute.DATASETS} />

                {/* Direct all metrics paths to metrics router */}
                <Route component={MetricsRouter} path={AppRoute.METRICS} />

                {/* No match found, render page not found */}
                <Route component={PageNotFoundPage} />
            </Switch>
        </Suspense>
    );
};
