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
    getDatasetsPath,
} from "../../utils/routes/routes.util";

const DatasetsAllPage = lazy(() =>
    import(
        /* webpackChunkName: "datasets-all-page" */ "../../pages/datasets-all-page/datasets-all-page.component"
    ).then((module) => ({ default: module.DatasetsAllPage }))
);

const DatasetsViewPage = lazy(() =>
    import(
        /* webpackChunkName: "datasets-view-page" */ "../../pages/datasets-view-page/datasets-view-page.component"
    ).then((module) => ({ default: module.DatasetsViewPage }))
);

const DatasetsOnboardPage = lazy(() =>
    import(
        /* webpackChunkName: "datasets-onboard-page" */ "../../pages/datasets-onboard-page/datasets-onboard-page.component"
    ).then((module) => ({ default: module.DatasetsOnboardPage }))
);

const DatasetsUpdatePage = lazy(() =>
    import(
        /* webpackChunkName: "datasets-update-page" */ "../../pages/datasets-update-page/datasets-update-page.component"
    ).then((module) => ({ default: module.DatasetsUpdatePage }))
);

const PageNotFoundPage = lazy(() =>
    import(
        /* webpackChunkName: "page-not-found-page" */ "../../pages/page-not-found-page/page-not-found-page.component"
    ).then((module) => ({ default: module.PageNotFoundPage }))
);

export const DatasetsRouter: FunctionComponent = () => {
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
                text: t("label.datasets"),
                onClick: () => navigate(getDatasetsPath()),
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
                {/* Datasets path */}
                {/* Redirect to datasets all path */}
                <Route
                    index
                    element={
                        <Navigate replace to={AppRouteRelative.DATASETS_ALL} />
                    }
                />

                {/* Datasets all path */}
                <Route
                    element={<DatasetsAllPage />}
                    path={AppRouteRelative.DATASETS_ALL}
                />

                {/* Datasets view path */}
                <Route
                    element={<DatasetsViewPage />}
                    path={AppRouteRelative.DATASETS_VIEW}
                />

                {/* Datasets onboard path */}
                <Route
                    element={<DatasetsOnboardPage />}
                    path={AppRouteRelative.DATASETS_ONBOARD}
                />

                {/* Datasets update path */}
                <Route
                    element={<DatasetsUpdatePage />}
                    path={AppRouteRelative.DATASETS_UPDATE}
                />

                {/* No match found, render page not found */}
                <Route element={<PageNotFoundPage />} path="*" />
            </Routes>
        </Suspense>
    );
};
