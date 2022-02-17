import React, { FunctionComponent, lazy, Suspense } from "react";
import { Route, Switch } from "react-router-dom";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import { AppRoute } from "../../utils/routes/routes.util";

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
    return (
        <Suspense fallback={<AppLoadingIndicatorV1 />}>
            <Switch>
                {/* Root cause for an anomaly index path. */}
                <Route
                    exact
                    component={RootCauseAnalysisForAnomalyIndexPage}
                    path={AppRoute.ROOT_CAUSE_ANALYSIS_FOR_ANOMALY_INDEX}
                />

                {/* Root cause for an anomaly path */}
                <Route
                    exact
                    component={RootCauseAnalysisForAnomalyPage}
                    path={AppRoute.ROOT_CAUSE_ANALYSIS_FOR_ANOMALY}
                />

                {/* No match found, render page not found */}
                <Route component={PageNotFoundPage} />
            </Switch>
        </Suspense>
    );
};
