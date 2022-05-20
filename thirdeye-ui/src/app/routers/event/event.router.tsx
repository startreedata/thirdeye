import React, { FunctionComponent, lazy, Suspense } from "react";
import { Navigate, Route, Routes } from "react-router-dom";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import { AppRouteRelative } from "../../utils/routes/routes.util";

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
                <Route
                    element={<EventsAllPage />}
                    path={AppRouteRelative.EVENTS_ALL}
                />

                {/* Events view page */}
                <Route
                    element={<EventsViewPage />}
                    path={AppRouteRelative.EVENTS_VIEW}
                />

                {/* No match found, render page not found */}
                <Route element={<PageNotFoundPage />} path="*" />
            </Routes>
        </Suspense>
    );
};
