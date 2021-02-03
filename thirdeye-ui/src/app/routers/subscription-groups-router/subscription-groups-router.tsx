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
    getConfigurationPath,
    getSubscriptionGroupsAllPath,
    getSubscriptionGroupsPath,
} from "../../utils/routes-util/routes-util";

const SubscriptionGroupsAllPage = lazy(() =>
    import(
        /* webpackChunkName: "subscription-groups-all-page" */ "../../pages/subscription-groups-all-page/subscription-groups-all-page.component"
    ).then((module) => ({ default: module.SubscriptionGroupsAllPage }))
);

const SubscriptionGroupsDetailPage = lazy(() =>
    import(
        /* webpackChunkName: "subscription-groups-detail-page" */ "../../pages/subscription-groups-detail-page/subscription-groups-detail-page.component"
    ).then((module) => ({ default: module.SubscriptionGroupsDetailPage }))
);

const SubscriptionGroupsCreatePage = lazy(() =>
    import(
        /* webpackChunkName: "subscription-groups-create-page" */ "../../pages/subscription-groups-create-page/subscription-groups-create-page.component"
    ).then((module) => ({ default: module.SubscriptionGroupsCreatePage }))
);

const SubscriptionGroupsUpdatePage = lazy(() =>
    import(
        /* webpackChunkName: "subscription-groups-update-page" */ "../../pages/subscription-groups-update-page/subscription-groups-update-page.component"
    ).then((module) => ({ default: module.SubscriptionGroupsUpdatePage }))
);

const PageNotFoundPage = lazy(() =>
    import(
        /* webpackChunkName: "page-not-found-page" */ "../../pages/page-not-found-page/page-not-found-page.component"
    ).then((module) => ({ default: module.PageNotFoundPage }))
);

export const SubscriptionGroupsRouter: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const { setRouterBreadcrumbs } = useAppBreadcrumbs();
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
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
        return <LoadingIndicator />;
    }

    return (
        <Suspense fallback={<LoadingIndicator />}>
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
        </Suspense>
    );
};
