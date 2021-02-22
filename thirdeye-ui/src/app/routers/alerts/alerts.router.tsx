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
    getAlertsAllPath,
    getAlertsPath,
} from "../../utils/routes/routes.util";

const AlertsAllPage = lazy(() =>
    import(
        /* webpackChunkName: "alerts-all-page" */ "../../pages/alerts-all-page/alerts-all-page.component"
    ).then((module) => ({ default: module.AlertsAllPage }))
);

const AlertsDetailPage = lazy(() =>
    import(
        /* webpackChunkName: "alerts-detail-page" */ "../../pages/alerts-detail-page/alerts-detail-page.component"
    ).then((module) => ({ default: module.AlertsDetailPage }))
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
    const [loading, setLoading] = useState(true);
    const { setRouterBreadcrumbs } = useAppBreadcrumbs();
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        setRouterBreadcrumbs([
            {
                text: t("label.alerts"),
                onClick: () => history.push(getAlertsPath()),
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

                {/* Alerts detail path */}
                <Route
                    exact
                    component={AlertsDetailPage}
                    path={AppRoute.ALERTS_DETAIL}
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
