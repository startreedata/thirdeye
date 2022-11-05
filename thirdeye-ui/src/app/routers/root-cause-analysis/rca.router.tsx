/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import React, { FunctionComponent, lazy, Suspense } from "react";
import { Route, Routes } from "react-router-dom";
import { TimeRangeQueryStringKey } from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import { InvestigationStateTracker } from "../../pages/root-cause-analysis-investigation-state-tracker/investigation-state-tracker.component";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import { RedirectValidation } from "../../utils/routes/redirect-validation/redirect-validation.component";
import { AppRouteRelative } from "../../utils/routes/routes.util";

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
            <Routes>
                <Route
                    element={<InvestigationStateTracker />}
                    path={`/${AppRouteRelative.ROOT_CAUSE_ANALYSIS_FOR_ANOMALY}/*`}
                >
                    {/* Root cause for an anomaly index path. */}
                    <Route
                        index
                        element={<RootCauseAnalysisForAnomalyIndexPage />}
                    />

                    {/* Root cause for an anomaly path */}
                    <Route
                        element={
                            <RedirectValidation
                                queryParams={[
                                    TimeRangeQueryStringKey.TIME_RANGE,
                                    TimeRangeQueryStringKey.START_TIME,
                                    TimeRangeQueryStringKey.END_TIME,
                                ]}
                                to=".."
                            >
                                <RootCauseAnalysisForAnomalyPage />
                            </RedirectValidation>
                        }
                        path={
                            AppRouteRelative.ROOT_CAUSE_ANALYSIS_FOR_ANOMALY_INVESTIGATE
                        }
                    />

                    <Route element={<PageNotFoundPage />} path="*" />
                </Route>

                {/* No match found, render page not found */}
                <Route element={<PageNotFoundPage />} path="*" />
            </Routes>
        </Suspense>
    );
};
