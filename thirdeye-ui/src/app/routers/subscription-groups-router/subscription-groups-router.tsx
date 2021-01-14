import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Redirect, Route, Switch, useHistory } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageNotFoundPage } from "../../pages/page-not-found-page/page-not-found-page.component";
import { SubscriptionGroupsAllPage } from "../../pages/subscription-groups-all-page/subscription-groups-all-page.component";
import { SubscriptionGroupsCreatePage } from "../../pages/subscription-groups-create-page/subscription-groups-create-page.component";
import { SubscriptionGroupsDetailPage } from "../../pages/subscription-groups-detail-page/subscription-groups-detail-page.component";
import { SubscriptionGroupsUpdatePage } from "../../pages/subscription-groups-update-page/subscription-groups-update-page.component";
import {
    AppRoute,
    getConfigurationPath,
    getSubscriptionGroupsAllPath,
    getSubscriptionGroupsPath,
} from "../../utils/routes-util/routes-util";

export const SubscriptionGroupsRouter: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const { setRouterBreadcrumbs } = useAppBreadcrumbs();
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        // Create router breadcrumbs
        setRouterBreadcrumbs([
            {
                text: t("label.configuration"),
                onClick: (): void => {
                    history.push(getConfigurationPath());
                },
            },
            {
                text: t("label.subscription-groups"),
                onClick: (): void => {
                    history.push(getSubscriptionGroupsPath());
                },
            },
        ]);

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
            {/* Subscription groups path */}
            <Route exact path={AppRoute.SUBSCRIPTION_GROUPS}>
                {/* Redirect to subscription groups all path */}
                <Redirect to={getSubscriptionGroupsAllPath()} />
            </Route>

            {/* Subscription groups all path */}
            <Route
                exact
                component={SubscriptionGroupsAllPage}
                path={AppRoute.SUBSCRIPTION_GROUPS_ALL}
            />

            {/* Subscription groups detail path */}
            <Route
                exact
                component={SubscriptionGroupsDetailPage}
                path={AppRoute.SUBSCRIPTION_GROUPS_DETAIL}
            />

            {/* Subscription groups create path */}
            <Route
                exact
                component={SubscriptionGroupsCreatePage}
                path={AppRoute.SUBSCRIPTION_GROUPS_CREATE}
            />

            {/* Subscription groups update path */}
            <Route
                exact
                component={SubscriptionGroupsUpdatePage}
                path={AppRoute.SUBSCRIPTION_GROUPS_UPDATE}
            />

            {/* No match found, render page not found */}
            <Route component={PageNotFoundPage} />
        </Switch>
    );
};
