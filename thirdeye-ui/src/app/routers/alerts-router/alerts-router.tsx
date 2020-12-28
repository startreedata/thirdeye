import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Redirect, Route, Switch } from "react-router-dom";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { AlertsAllPage } from "../../pages/alerts-all-page/alerts-all-page.component";
import { AlertsCreatePage } from "../../pages/alerts-create-page/alerts-create-page.component";
import { AlertsDetailPage } from "../../pages/alerts-detail-page/alerts-detail-page.component";
import { AlertsUpdatePage } from "../../pages/alerts-update-page/alerts-update-page.component";
import { PageNotFoundPage } from "../../pages/page-not-found-page/page-not-found-page.component";
import { useAppBreadcrumbsStore } from "../../store/app-breadcrumbs-store/app-breadcrumbs-store";
import { useAppToolbarStore } from "../../store/app-toolbar-store/app-toolbar-store";
import {
    AppRoute,
    getAlertsAllPath,
    getAlertsPath,
} from "../../utils/routes-util/routes-util";

export const AlertsRouter: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [setAppSectionBreadcrumbs] = useAppBreadcrumbsStore((state) => [
        state.setAppSectionBreadcrumbs,
    ]);
    const [removeAppToolbar] = useAppToolbarStore((state) => [
        state.removeAppToolbar,
    ]);
    const { t } = useTranslation();

    useEffect(() => {
        // Create app section breadcrumbs
        setAppSectionBreadcrumbs([
            {
                text: t("label.alerts"),
                pathFn: getAlertsPath,
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
        <Switch>
            {/* Alerts path */}
            <Route exact path={AppRoute.ALERTS}>
                {/* Redirect to alerts all path */}
                <Redirect to={getAlertsAllPath()} />
            </Route>

            {/* Alerts all path */}
            <Route exact component={AlertsAllPage} path={AppRoute.ALERTS_ALL} />

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
    );
};
