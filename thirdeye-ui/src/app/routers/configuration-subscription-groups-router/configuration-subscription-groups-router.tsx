import React, {
    FunctionComponent,
    ReactNode,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { Redirect, Route, RouteComponentProps, Switch } from "react-router-dom";
import { ConfigurationToolbar } from "../../components/configuration-toolbar/configuration-toolbar.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { ConfigurationSubscriptionGroupsAllPage } from "../../pages/configuration-subscription-groups-all-page/configuration-subscription-groups-all-page.component";
import { ConfigurationSubscriptionGroupsCreatePage } from "../../pages/configuration-subscription-groups-create-page/configuration-subscription-groups-create-page.component";
import { ConfigurationSubscriptionGroupsDetailPage } from "../../pages/configuration-subscription-groups-detail-page/configuration-subscription-groups-detail-page.component";
import { ConfigurationSubscriptionGroupsUpdatePage } from "../../pages/configuration-subscription-groups-update-page/configuration-subscription-groups-update-page.component";
import { PageNotFoundPage } from "../../pages/page-not-found-page/page-not-found-page.component";
import { useAppBreadcrumbsStore } from "../../store/app-breadcrumbs-store/app-breadcrumbs-store";
import {
    AppRoute,
    getConfigurationSubscriptionGroupsAllPath,
    getConfigurationSubscriptionGroupsPath,
} from "../../utils/routes-util/routes-util";

export const ConfigurationSubscriptionGroupsRouter: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const [setAppSectionBreadcrumbs] = useAppBreadcrumbsStore((state) => [
        state.setAppSectionBreadcrumbs,
    ]);
    const { t } = useTranslation();

    useEffect(() => {
        // Create app section breadcrumbs
        setAppSectionBreadcrumbs([
            {
                text: t("label.configuration"),
                path: getConfigurationSubscriptionGroupsPath(),
            },
            {
                text: t("label.subscription-groups"),
                path: getConfigurationSubscriptionGroupsPath(),
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
            {/* Configuration subscription groups path */}
            <Route exact path={AppRoute.CONFIGURATION_SUBSCRIPTION_GROUPS}>
                {/* Redirect to configuration subscription groups all path */}
                <Redirect to={getConfigurationSubscriptionGroupsAllPath()} />
            </Route>

            {/* Configuration subscription groups all path */}
            <Route
                exact
                component={ConfigurationSubscriptionGroupsAllPage}
                path={AppRoute.CONFIGURATION_SUBSCRIPTION_GROUPS_ALL}
            />

            {/* Configuration subscription groups detail path */}
            <Route
                exact
                component={ConfigurationSubscriptionGroupsDetailPage}
                path={AppRoute.CONFIGURATION_SUBSCRIPTION_GROUPS_DETAIL}
            />

            {/* Configuration subscription groups create path */}
            <Route
                exact
                component={ConfigurationSubscriptionGroupsCreatePage}
                path={AppRoute.CONFIGURATION_SUBSCRIPTION_GROUPS_CREATE}
            />

            {/* Configuration subscription groups update path */}
            <Route
                exact
                component={ConfigurationSubscriptionGroupsUpdatePage}
                path={AppRoute.CONFIGURATION_SUBSCRIPTION_GROUPS_UPDATE}
            />

            {/* No match found, render page not found with configuration toolbar */}
            <Route
                render={(props: RouteComponentProps): ReactNode => (
                    <PageNotFoundPage
                        {...props}
                        pageContainerToolbar={<ConfigurationToolbar />}
                    />
                )}
            />
        </Switch>
    );
};
