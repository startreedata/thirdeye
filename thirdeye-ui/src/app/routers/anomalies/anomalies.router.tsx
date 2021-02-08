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
    getAnomaliesAllPath,
    getAnomaliesPath,
} from "../../utils/routes/routes.util";

const AnomaliesAllPage = lazy(() =>
    import(
        /* webpackChunkName: "anomalies-all-page" */ "../../pages/anomalies-all-page/anomalies-all-page.component"
    ).then((module) => ({ default: module.AnomaliesAllPage }))
);

const AnomaliesDetailPage = lazy(() =>
    import(
        /* webpackChunkName: "anomalies-detail-page" */ "../../pages/anomalies-detail-page/anomalies-detail-page.component"
    ).then((module) => ({ default: module.AnomaliesDetailPage }))
);

const PageNotFoundPage = lazy(() =>
    import(
        /* webpackChunkName: "page-not-found-page" */ "../../pages/page-not-found-page/page-not-found-page.component"
    ).then((module) => ({ default: module.PageNotFoundPage }))
);

export const AnomaliesRouter: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const { setRouterBreadcrumbs } = useAppBreadcrumbs();
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        setRouterBreadcrumbs([
            {
                text: t("label.anomalies"),
                onClick: () => history.push(getAnomaliesPath()),
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
                {/* Anomalies path */}
                <Route exact path={AppRoute.ANOMALIES}>
                    {/* Redirect to anomalies all path */}
                    <Redirect to={getAnomaliesAllPath()} />
                </Route>

                {/* Anomalies all path */}
                <Route
                    exact
                    component={AnomaliesAllPage}
                    path={AppRoute.ANOMALIES_ALL}
                />

                {/* Anomalies detail path */}
                <Route
                    exact
                    component={AnomaliesDetailPage}
                    path={AppRoute.ANOMALIES_DETAIL}
                />

                {/* No match found, render page not found */}
                <Route component={PageNotFoundPage} />
            </Switch>
        </Suspense>
    );
};
