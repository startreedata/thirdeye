import React, { FunctionComponent, lazy, Suspense } from "react";
import { Redirect, Route, Switch } from "react-router-dom";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import {
    AppRoute,
    getSubscriptionGroupsAllPath,
} from "../../utils/routes/routes.util";

const SubscriptionGroupsAllPage = lazy(() =>
    import(
        /* webpackChunkName: "subscription-groups-all-page" */ "../../pages/subscription-groups-all-page/subscription-groups-all-page.component"
    ).then((module) => ({ default: module.SubscriptionGroupsAllPage }))
);

const SubscriptionGroupsViewPage = lazy(() =>
    import(
        /* webpackChunkName: "subscription-groups-view-page" */ "../../pages/subscription-groups-view-page/subscription-groups-view-page.component"
    ).then((module) => ({ default: module.SubscriptionGroupsViewPage }))
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
    return (
        <Suspense fallback={<AppLoadingIndicatorV1 />}>
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

                {/* Subscription groups view path */}
                <Route
                    exact
                    component={SubscriptionGroupsViewPage}
                    path={AppRoute.SUBSCRIPTION_GROUPS_VIEW}
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
