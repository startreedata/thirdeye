import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Redirect, Route, Switch } from "react-router-dom";
import { PageContainer } from "../components/page-container/page-container.component";
import { PageLoadingIndicator } from "../components/page-loading-indicator/page-loading-indicator.component";
import { AlertsAllPage } from "../pages/alerts-all-page/alerts-all-page.component";
import { AlertsCreatePage } from "../pages/alerts-create-page/alerts-create-page.component";
import { AlertsDetailPage } from "../pages/alerts-detail-page/alerts-detail-page.component";
import { AlertsUpdatePage } from "../pages/alerts-update-page/alerts-update-page.component";
import { useApplicationBreadcrumbsStore } from "../store/application-breadcrumbs/application-breadcrumbs.store";
import {
    ApplicationRoute,
    getAlertsAllPath,
    getAlertsPath,
    getPageNotFoundPath,
} from "../utils/route/routes-util";

export const AlertsRouter: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [setRouterBreadcrumb] = useApplicationBreadcrumbsStore((state) => [
        state.setRouterBreadcrumb,
    ]);
    const { t } = useTranslation();

    useEffect(() => {
        // Create router breadcrumb
        setRouterBreadcrumb({
            text: t("label.alerts"),
            path: getAlertsPath(),
        });

        setLoading(false);
    }, [setRouterBreadcrumb, t]);

    if (loading) {
        return (
            <PageContainer>
                <PageLoadingIndicator />
            </PageContainer>
        );
    }

    return (
        <Switch>
            {/* Alerts path */}
            <Route exact path={ApplicationRoute.ALERTS}>
                {/* Redirect to alerts all path */}
                <Redirect to={getAlertsAllPath()} />
            </Route>

            {/* Alerts all path */}
            <Route
                exact
                component={AlertsAllPage}
                path={ApplicationRoute.ALERTS_ALL}
            />

            {/* Alerts detail path */}
            <Route
                exact
                component={AlertsDetailPage}
                path={ApplicationRoute.ALERTS_DETAIL}
            />

            {/* Alerts create path */}
            <Route
                exact
                component={AlertsCreatePage}
                path={ApplicationRoute.ALERTS_CREATE}
            />

            {/* Alerts update path */}
            <Route
                exact
                component={AlertsUpdatePage}
                path={ApplicationRoute.ALERTS_UPDATE}
            />

            {/* No match found, redirect to page not found path */}
            <Redirect to={getPageNotFoundPath()} />
        </Switch>
    );
};
