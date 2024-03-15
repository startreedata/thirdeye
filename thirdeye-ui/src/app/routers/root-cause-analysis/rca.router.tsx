/*
 * Copyright 2023 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { Grid } from "@material-ui/core";
import React, { FunctionComponent, lazy, Suspense } from "react";
import { Navigate, Route, Routes, useSearchParams } from "react-router-dom";
import { InvestigationStepsNavigation } from "../../components/rca/investigation-steps-navigation/investigation-steps-navigation.component";
import { TimeRangeQueryStringKey } from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
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

const InvestigationStateTracker = lazy(() =>
    import(
        /* webpackChunkName: "investigation-state-tracker-container-page" */ "../../pages/root-cause-analysis-investigation-state-tracker/investigation-state-tracker.component"
    ).then((module) => ({ default: module.InvestigationStateTracker }))
);

const InvestigationStateTrackerV2 = lazy(() =>
    import(
        /* webpackChunkName: "investigation-state-tracker-container-page-v2" */ "../../pages/rca/investigation-state-tracker-container-page/investigation-state-tracker.component"
    ).then((module) => ({ default: module.InvestigationStateTracker }))
);

const IndexPage = lazy(() =>
    import(
        /* webpackChunkName: "index-page" */ "../../pages/rca/index-page/index-page.component"
    ).then((module) => ({ default: module.IndexPage }))
);

const WhatWherePage = lazy(() =>
    import(
        /* webpackChunkName: "what-where-page" */ "../../pages/rca/what-where-page/what-where-page.component"
    ).then((module) => ({ default: module.WhatWherePage }))
);

const EventsPage = lazy(() =>
    import(
        /* webpackChunkName: "events-page" */ "../../pages/rca/events-page/events-page.component"
    ).then((module) => ({ default: module.EventsPage }))
);

const ReviewShareSavePage = lazy(() =>
    import(
        /* webpackChunkName: "review-share-save-page" */ "../../pages/rca/review-share-save-page/review-share-save-page.component"
    ).then((module) => ({ default: module.ReviewShareSavePage }))
);

const PageNotFoundPage = lazy(() =>
    import(
        /* webpackChunkName: "page-not-found-page" */ "../../pages/page-not-found-page/page-not-found-page.component"
    ).then((module) => ({ default: module.PageNotFoundPage }))
);

export const RootCauseAnalysisRouter: FunctionComponent = () => {
    const [searchParams] = useSearchParams();

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

                <Route
                    element={<InvestigationStateTrackerV2 />}
                    path={`/${AppRouteRelative.ROOT_CAUSE_ANALYSIS_FOR_ANOMALY_V2}/*`}
                >
                    {/* Root cause for an anomaly index path. */}
                    <Route index element={<IndexPage />} />

                    {/* Root cause for an anomaly path */}
                    <Route
                        element={
                            <RedirectValidation
                                useOutlet
                                queryParams={[
                                    TimeRangeQueryStringKey.START_TIME,
                                    TimeRangeQueryStringKey.END_TIME,
                                ]}
                                to=".."
                            >
                                <Grid item xs={12}>
                                    <InvestigationStepsNavigation />
                                </Grid>
                            </RedirectValidation>
                        }
                        path={`${AppRouteRelative.ROOT_CAUSE_ANALYSIS_FOR_ANOMALY_INVESTIGATE}/*`}
                    >
                        {/* Root cause for an anomaly index path. */}
                        <Route
                            index
                            element={
                                <Navigate
                                    replace
                                    to={`${
                                        AppRouteRelative.RCA_WHAT_WHERE
                                    }?${searchParams.toString()}`}
                                />
                            }
                        />
                        <Route
                            element={<WhatWherePage />}
                            path={AppRouteRelative.RCA_WHAT_WHERE}
                        >
                            <Route
                                element={
                                    <Navigate
                                        replace
                                        to={`${
                                            AppRouteRelative.RCA_WHAT_WHERE
                                        }?${searchParams.toString()}`}
                                    />
                                }
                                path="*"
                            />
                        </Route>
                        <Route
                            element={<EventsPage />}
                            path={AppRouteRelative.RCA_EVENTS}
                        />
                        <Route
                            element={<ReviewShareSavePage />}
                            path={AppRouteRelative.RCA_REVIEW_SHARE}
                        />
                    </Route>
                    <Route element={<PageNotFoundPage />} path="*" />
                </Route>

                {/* No match found, render page not found */}
                <Route element={<PageNotFoundPage />} path="*" />
            </Routes>
        </Suspense>
    );
};
