import React, {
    FunctionComponent,
    ReactNode,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { Redirect, Route, RouteComponentProps, Switch } from "react-router-dom";
import { AppToolbarConfiguration } from "../../components/app-toolbar-configuration/app-toolbar-configuration.component";
import { LoadingIndicator } from "../../components/loading-indicator/loading-indicator.component";
import { PageContainer } from "../../components/page-container/page-container.component";
import { PageNotFoundPage } from "../../pages/page-not-found-page/page-not-found-page.component";
import { useAppBreadcrumbsStore } from "../../store/app-breadcrumbs-store/app-breadcrumbs-store";
import {
    AppRoute,
    getConfigurationPath,
    getConfigurationSubscriptionGroupsPath,
} from "../../utils/routes-util/routes-util";
import { ConfigurationSubscriptionGroupsRouter } from "../configuration-subscription-groups-router/configuration-subscription-groups-router";

export const ConfigurationRouter: FunctionComponent = () => {
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
                pathFn: getConfigurationPath,
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
            {/* Configuration path */}
            <Route exact path={AppRoute.CONFIGURATION}>
                {/* Redirect to configuration subscription groups path */}
                <Redirect to={getConfigurationSubscriptionGroupsPath()} />
            </Route>

            {/* Direct all configuration subscription groups paths to configuration subscription groups
            router */}
            <Route
                component={ConfigurationSubscriptionGroupsRouter}
                path={AppRoute.CONFIGURATION_SUBSCRIPTION_GROUPS}
            />

            {/* No match found, render page not found with configuration toolbar */}
            <Route
                render={(props: RouteComponentProps): ReactNode => (
                    <PageNotFoundPage
                        {...props}
                        appToolbar={<AppToolbarConfiguration />}
                    />
                )}
            />
        </Switch>
    );
};
