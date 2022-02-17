import React, { FunctionComponent, lazy, Suspense } from "react";
import { Redirect, Route, Switch } from "react-router-dom";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import { AppRoute, getDatasetsAllPath } from "../../utils/routes/routes.util";

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
    return (
        <Suspense fallback={<AppLoadingIndicatorV1 />}>
            <Switch>
                {/* Datasets path */}
                <Route exact path={AppRoute.DATASETS}>
                    {/* Redirect to datasets all path */}
                    <Redirect to={getDatasetsAllPath()} />
                </Route>

                {/* Datasets all path */}
                <Route
                    exact
                    component={DatasetsAllPage}
                    path={AppRoute.DATASETS_ALL}
                />

                {/* Datasets view path */}
                <Route
                    exact
                    component={DatasetsViewPage}
                    path={AppRoute.DATASETS_VIEW}
                />

                {/* Datasets onboard path */}
                <Route
                    exact
                    component={DatasetsOnboardPage}
                    path={AppRoute.DATASETS_ONBOARD}
                />

                {/* Datasets update path */}
                <Route
                    exact
                    component={DatasetsUpdatePage}
                    path={AppRoute.DATASETS_UPDATE}
                />

                {/* No match found, render page not found */}
                <Route component={PageNotFoundPage} />
            </Switch>
        </Suspense>
    );
};
