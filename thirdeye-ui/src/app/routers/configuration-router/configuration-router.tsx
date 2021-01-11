import React, { FunctionComponent, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { Redirect, Route, Switch } from "react-router-dom";
import { AppToolbarConfiguration } from "../../components/app-toolbar-configuration/app-toolbar-configuration.component";
import { PageNotFoundPage } from "../../pages/page-not-found-page/page-not-found-page.component";
import { useAppBreadcrumbsStore } from "../../store/app-breadcrumbs-store/app-breadcrumbs-store";
import { useAppToolbarStore } from "../../store/app-toolbar-store/app-toolbar-store";
import {
    AppRoute,
    getConfigurationPath,
    getSubscriptionGroupsPath,
} from "../../utils/routes-util/routes-util";
import { SubscriptionGroupsRouter } from "../subscription-groups-router/subscription-groups-router";

export const ConfigurationRouter: FunctionComponent = () => {
    const [setAppSectionBreadcrumbs] = useAppBreadcrumbsStore((state) => [
        state.setAppSectionBreadcrumbs,
    ]);
    const [setAppToolbar] = useAppToolbarStore((state) => [
        state.setAppToolbar,
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

        // Configuration app toolbar under this router
        setAppToolbar(<AppToolbarConfiguration />);
    }, []);

    return (
        <Switch>
            {/* Configuration path */}
            <Route exact path={AppRoute.CONFIGURATION}>
                {/* Redirect to subscription groups path */}
                <Redirect to={getSubscriptionGroupsPath()} />
            </Route>

            {/* Direct all subscription groups paths to subscription groups router */}
            <Route
                component={SubscriptionGroupsRouter}
                path={AppRoute.SUBSCRIPTION_GROUPS}
            />

            {/* No match found, render page not found */}
            <Route component={PageNotFoundPage} />
        </Switch>
    );
};
