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
import { PageContainer } from "../../components/page-container/page-container.component";
import { useAppToolbarStore } from "../../store/app-toolbar-store/app-toolbar-store";
import {
    AppRoute,
    getAlertsAllPath,
    getAlertsPath,
} from "../../utils/routes-util/routes-util";

const AlertsAllPage = lazy(() =>
    import(
        /* webpackChunkName: 'AlertsAllPage' */

        "../../pages/alerts-all-page/alerts-all-page.component"
    ).then(({ AlertsAllPage }) => ({
        default: AlertsAllPage,
    }))
);

const AlertsCreatePage = lazy(() =>
    import(
        /* webpackChunkName: 'AlertsCreatePage' */

        "../../pages/alerts-create-page/alerts-create-page.component"
    ).then(({ AlertsCreatePage }) => ({
        default: AlertsCreatePage,
    }))
);

const AlertsDetailPage = lazy(() =>
    import(
        /* webpackChunkName: 'AlertsDetailPage' */

        "../../pages/alerts-detail-page/alerts-detail-page.component"
    ).then(({ AlertsDetailPage }) => ({
        default: AlertsDetailPage,
    }))
);

const AlertsUpdatePage = lazy(() =>
    import(
        /* webpackChunkName: 'AlertsUpdatePage' */
        "../../pages/alerts-update-page/alerts-update-page.component"
    ).then(({ AlertsUpdatePage }) => ({
        default: AlertsUpdatePage,
    }))
);

const PageNotFoundPage = lazy(() =>
    import(
        /* webpackChunkName: 'PageNotFoundPage' */

        "../../pages/page-not-found-page/page-not-found-page.component"
    ).then(({ PageNotFoundPage }) => ({
        default: PageNotFoundPage,
    }))
);

export const AlertsRouter: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const { setRouterBreadcrumbs } = useAppBreadcrumbs();
    const [removeAppToolbar] = useAppToolbarStore((state) => [
        state.removeAppToolbar,
    ]);
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        // Create router breadcrumbs
        setRouterBreadcrumbs([
            {
                text: t("label.alerts"),
                onClick: (): void => {
                    history.push(getAlertsPath());
                },
            },
        ]);

        // No app toolbar under this router
        removeAppToolbar();

        setLoading(false);
    }, []);

    if (loading) {
        return (
            <PageContainer>
                <LoadingIndicator />
            </PageContainer>
        );
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
