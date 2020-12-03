import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Redirect, Route, Switch } from "react-router-dom";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageLoadingIndicator } from "../../components/page-loading-indicator/page-loading-indicator.component";
import { AlertsAllPage } from "../../pages/alerts-all/alerts-all-page.component";
import { AlertsCreatePage } from "../../pages/alerts-create/alerts-create-page.component";
import { AlertsDetailPage } from "../../pages/alerts-detail/alerts-detail-page.component";
import { AlertsUpdatePage } from "../../pages/alerts-update/alerts-update-page.component";
import { PageNotFoundPage } from "../../pages/page-not-found/page-not-found-page.component";
import { useApplicationBreadcrumbsStore } from "../../store/application-breadcrumbs/application-breadcrumbs-store";
import {
    ApplicationRoute,
    getAlertsAllPath,
    getAlertsPath,
} from "../../utils/routes/routes-util";

export const AlertsRouter: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [
        setAppSectionBreadcrumb,
    ] = useApplicationBreadcrumbsStore((state) => [
        state.setAppSectionBreadcrumb,
    ]);
    const { t } = useTranslation();

    useEffect(() => {
        // Create app section breadcrumb
        setAppSectionBreadcrumb({
            text: t("label.alerts"),
            path: getAlertsPath(),
        });

        setLoading(false);
    }, []);

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

            {/* No match found, render page not found */}
            <Route component={PageNotFoundPage} />
        </Switch>
    );
};
