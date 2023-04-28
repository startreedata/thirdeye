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
import React, { FunctionComponent, lazy, Suspense } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { TimeRangeQueryStringKey } from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import { RedirectValidation } from "../../utils/routes/redirect-validation/redirect-validation.component";
import { RedirectWithDefaultParams } from "../../utils/routes/redirect-with-default-params/redirect-with-default-params.component";
import {
    AppRouteRelative,
    generateDateRangeMonthsFromNow,
} from "../../utils/routes/routes.util";
import { SaveLastUsedSearchParams } from "../../utils/routes/save-last-used-search-params/save-last-used-search-params.component";

const EventsAllPage = lazy(() =>
    import(
        /* webpackChunkName: "events-all-page" */ "../../pages/event-all-page/events-all-page.component"
    ).then((module) => ({ default: module.EventsAllPage }))
);

const EventsViewPage = lazy(() =>
    import(
        /* webpackChunkName: "events-view-page" */ "../../pages/events-view-page/events-view-page.component"
    ).then((module) => ({ default: module.EventsViewPage }))
);

const EventsCreatePage = lazy(() =>
    import(
        /* webpackChunkName: "events-view-page" */
        "../../pages/events-create-page/events-create-page.component"
    ).then((module) => ({ default: module.EventsCreatePage }))
);

const PageNotFoundPage = lazy(() =>
    import(
        /* webpackChunkName: "page-not-found-page" */ "../../pages/page-not-found-page/page-not-found-page.component"
    ).then((module) => ({ default: module.PageNotFoundPage }))
);

export const EventRouter: FunctionComponent = () => {
    return (
        <Suspense fallback={<AppLoadingIndicatorV1 />}>
            <Routes>
                <Route
                    index
                    element={
                        <Navigate replace to={AppRouteRelative.EVENTS_ALL} />
                    }
                />
                {/* Events all path */}
                <Route path={AppRouteRelative.EVENTS_ALL}>
                    <Route
                        index
                        element={
                            <RedirectWithDefaultParams
                                replace
                                useStoredLastUsedParamsPathKey
                                customDurationGenerator={() => {
                                    return generateDateRangeMonthsFromNow(3);
                                }}
                                to={AppRouteRelative.EVENTS_ALL_RANGE}
                            />
                        }
                    />
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
                                <SaveLastUsedSearchParams>
                                    <EventsAllPage />
                                </SaveLastUsedSearchParams>
                            </RedirectValidation>
                        }
                        path={AppRouteRelative.EVENTS_ALL_RANGE}
                    />
                </Route>

                {/* Events view page */}
                <Route
                    element={<EventsViewPage />}
                    path={AppRouteRelative.EVENTS_VIEW}
                />

                {/* Events create page */}
                <Route
                    element={<EventsCreatePage />}
                    path={AppRouteRelative.EVENTS_CREATE}
                />

                {/* No match found, render page not found */}
                <Route element={<PageNotFoundPage />} path="*" />
            </Routes>
        </Suspense>
    );
};
