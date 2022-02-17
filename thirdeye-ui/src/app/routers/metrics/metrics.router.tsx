import React, {
    FunctionComponent,
    lazy,
    Suspense,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { Redirect, Route, Switch, useHistory } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import {
    AppRoute,
    getConfigurationPath,
    getMetricsAllPath,
    getMetricsPath,
} from "../../utils/routes/routes.util";

const MetricsAllPage = lazy(() =>
    import(
        /* webpackChunkName: "metrics-all-page" */ "../../pages/metrics-all-page/metrics-all-page.component"
    ).then((module) => ({ default: module.MetricsAllPage }))
);

const MetricsViewPage = lazy(() =>
    import(
        /* webpackChunkName: "metrics-view-page" */ "../../pages/metrics-view-page/metrics-view-page.component"
    ).then((module) => ({ default: module.MetricsViewPage }))
);

const PageNotFoundPage = lazy(() =>
    import(
        /* webpackChunkName: "page-not-found-page" */ "../../pages/page-not-found-page/page-not-found-page.component"
    ).then((module) => ({ default: module.PageNotFoundPage }))
);

const MetricsCreatePage = lazy(() =>
    import(
        /* webpackChunkName: "metrics-create-page" */ "../../pages/metrics-create-page/metrics-create-page.component"
    ).then((module) => ({ default: module.MetricsCreatePage }))
);

const MetricsUpdatePage = lazy(() =>
    import(
        /* webpackChunkName: "metrics-update-page" */ "../../pages/metrics-update-page/metrics-update-page.component"
    ).then((module) => ({ default: module.MetricsUpdatePage }))
);

export const MetricsRouter: FunctionComponent = () => {
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
            {
                text: t("label.metrics"),
                onClick: () => history.push(getMetricsPath()),
            },
        ]);
        setLoading(false);
    }, []);

    if (loading) {
        return <AppLoadingIndicatorV1 />;
    }

    return (
        <Suspense fallback={<AppLoadingIndicatorV1 />}>
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

                {/* Metrics view path */}
                <Route
                    exact
                    component={MetricsViewPage}
                    path={AppRoute.METRICS_VIEW}
                />

                {/* Metrics create path */}
                <Route
                    exact
                    component={MetricsCreatePage}
                    path={AppRoute.METRICS_CREATE}
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
