import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Redirect, Route, Switch } from "react-router-dom";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageLoadingIndicator } from "../../components/page-loading-indicator/page-loading-indicator.component";
import { AnomaliesAllPage } from "../../pages/anomalies-all/anomalies-all-page.component";
import { AnomaliesDetailPage } from "../../pages/anomalies-detail/anomalies-detail-page.component";
import { PageNotFoundPage } from "../../pages/page-not-found/page-not-found-page.component";
import { useApplicationBreadcrumbsStore } from "../../store/application-breadcrumbs/application-breadcrumbs-store";
import {
    ApplicationRoute,
    getAnomaliesAllPath,
    getAnomaliesPath,
} from "../../utils/routes/routes-util";

export const AnomaliesRouter: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [
        setAppSectionBreadcrumb,
    ] = useApplicationBreadcrumbsStore((state) => [
        state.setAppSectionBreadcrumb,
    ]);
    const { t } = useTranslation();

    useEffect(() => {
        // Create router breadcrumb
        setAppSectionBreadcrumb({
            text: t("label.anomalies"),
            path: getAnomaliesPath(),
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
            {/* Anomalies path */}
            <Route exact path={ApplicationRoute.ANOMALIES}>
                {/* Redirect to anomalies all path */}
                <Redirect to={getAnomaliesAllPath()} />
            </Route>

            {/* Anomalies all path */}
            <Route
                exact
                component={AnomaliesAllPage}
                path={ApplicationRoute.ANOMALIES_ALL}
            />

            {/* Anomalies detail path */}
            <Route
                exact
                component={AnomaliesDetailPage}
                path={ApplicationRoute.ANOMALIES_DETAIL}
            />

            {/* No match found, render page not found */}
            <Route component={PageNotFoundPage} />
        </Switch>
    );
};
