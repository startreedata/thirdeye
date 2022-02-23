import React, {
    FunctionComponent,
    lazy,
    Suspense,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { Route, Routes, useNavigate } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import { AppRoute, AppRouteRelative } from "../../utils/routes/routes.util";

const RootCauseAnalysisForAnomalyIndexPage = lazy(() =>
    import(
        /* webpackChunkName: "root-analysis-for-anomaly-index-page" */ "../../pages/root-cause-analysis-for-anomaly-index-page/root-cause-analysis-for-anomaly-index-page.component"
    ).then((module) => ({
        default: module.RootCauseAnalysisForAnomalyIndexPage,
    }))
);

const RootCauseAnalysisForAnomalyPage = lazy(() =>
    import(
        /* webpackChunkName: "root-analysis-for-anomaly-page" */ "../../pages/root-cause-analysis-for-anomaly-page/root-cause-analysis-for-anomaly-page.component"
    ).then((module) => ({ default: module.RootCauseAnalysisForAnomalyPage }))
);

const PageNotFoundPage = lazy(() =>
    import(
        /* webpackChunkName: "page-not-found-page" */ "../../pages/page-not-found-page/page-not-found-page.component"
    ).then((module) => ({ default: module.PageNotFoundPage }))
);

export const RootCauseAnalysisRouter: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const { setRouterBreadcrumbs } = useAppBreadcrumbs();
    const navigate = useNavigate();
    const { t } = useTranslation();

    useEffect(() => {
        setRouterBreadcrumbs([
            {
                text: t("label.root-cause-analysis"),
                onClick: () =>
                    navigate(AppRoute.ROOT_CAUSE_ANALYSIS_FOR_ANOMALY),
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
                {/* Root cause for an anomaly index path. */}
                <Route
                    element={<RootCauseAnalysisForAnomalyIndexPage />}
                    path={
                        AppRouteRelative.ROOT_CAUSE_ANALYSIS_FOR_ANOMALY_INDEX
                    }
                />

                {/* Root cause for an anomaly path */}
                <Route
                    element={<RootCauseAnalysisForAnomalyPage />}
                    path={AppRouteRelative.ROOT_CAUSE_ANALYSIS_FOR_ANOMALY}
                />

                {/* No match found, render page not found */}
                <Route element={<PageNotFoundPage />} path="*" />
            </Routes>
        </Suspense>
    );
};
