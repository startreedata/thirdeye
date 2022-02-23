import React, {
    FunctionComponent,
    lazy,
    Suspense,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { Navigate, Route, Routes, useNavigate } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import {
    AppRouteRelative,
    getConfigurationPath,
    getSubscriptionGroupsAllPath,
    getSubscriptionGroupsPath,
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
    const [loading, setLoading] = useState(true);
    const { setRouterBreadcrumbs } = useAppBreadcrumbs();
    const navigate = useNavigate();
    const { t } = useTranslation();

    useEffect(() => {
        setRouterBreadcrumbs([
            {
                text: t("label.configuration"),
                onClick: () => navigate(getConfigurationPath()),
            },
            {
                text: t("label.subscription-groups"),
                onClick: () => navigate(getSubscriptionGroupsPath()),
            },
        ]);
        setLoading(false);
    }, []);

    if (loading) {
        return <AppLoadingIndicatorV1 />;
    }

    return (
        <Suspense fallback={<AppLoadingIndicatorV1 />}>
            <Routes>
                {/* Subscription groups path */}
                {/* Redirect to subscription groups all path */}
                <Route
                    index
                    element={
                        <Navigate replace to={getSubscriptionGroupsAllPath()} />
                    }
                />

                {/* Subscription groups all path */}
                <Route
                    element={<SubscriptionGroupsAllPage />}
                    path={AppRouteRelative.SUBSCRIPTION_GROUPS_ALL}
                />

                {/* Subscription groups view path */}
                <Route
                    element={<SubscriptionGroupsViewPage />}
                    path={AppRouteRelative.SUBSCRIPTION_GROUPS_VIEW}
                />

                {/* Subscription groups create path */}
                <Route
                    element={<SubscriptionGroupsCreatePage />}
                    path={AppRouteRelative.SUBSCRIPTION_GROUPS_CREATE}
                />

                {/* Subscription groups update path */}
                <Route
                    element={<SubscriptionGroupsUpdatePage />}
                    path={AppRouteRelative.SUBSCRIPTION_GROUPS_UPDATE}
                />

                {/* No match found, render page not found */}
                <Route element={<PageNotFoundPage />} path="*" />
            </Routes>
        </Suspense>
    );
};
