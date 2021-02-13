import React, {
    FunctionComponent,
    lazy,
    Suspense,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { Redirect, Route, Switch, useHistory } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
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

const MetricsDetailPage = lazy(() =>
    import(
        /* webpackChunkName: "metrics-detail-page" */ "../../pages/metrics-detail-page/metrics-detail-page.component"
    ).then((module) => ({ default: module.MetricsDetailPage }))
);

const PageNotFoundPage = lazy(() =>
    import(
        /* webpackChunkName: "page-not-found-page" */ "../../pages/page-not-found-page/page-not-found-page.component"
    ).then((module) => ({ default: module.PageNotFoundPage }))
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
        return <LoadingIndicator />;
    }

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

                {/* Metrics detail path */}
                <Route
                    exact
                    component={MetricsDetailPage}
                    path={AppRoute.METRICS_DETAIL}
                />

                {/* No match found, render page not found */}
                <Route component={PageNotFoundPage} />
            </Switch>
        </Suspense>
    );
};
