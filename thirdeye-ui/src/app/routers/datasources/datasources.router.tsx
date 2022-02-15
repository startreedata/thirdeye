import React, {
    FunctionComponent,
    lazy,
    Suspense,
    useEffect,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { Redirect, Route, Switch, useHistory } from "react-router-dom";
import { useAppBreadcrumbs } from "../../components/app-breadcrumbs/app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { AppLoadingIndicatorV1 } from "../../platform/components";
import {
    AppRoute,
    getDatasourcesAllPath,
    getDatasourcesPath,
} from "../../utils/routes/routes.util";

const DatasourcesAllPage = lazy(() =>
    import(
        /* webpackChunkName: "datasources-all-page" */ "../../pages/datasources-all-page/datasources-all-page.component"
    ).then((module) => ({ default: module.DatasourcesAllPage }))
);

const DatasourcesViewPage = lazy(() =>
    import(
        /* webpackChunkName: "datasources-view-page" */ "../../pages/datasources-view-page/datasources-view-page.component"
    ).then((module) => ({ default: module.DatasourcesViewPage }))
);

const DatasourcesCreatePage = lazy(() =>
    import(
        /* webpackChunkName: "datasources-create-page" */ "../../pages/datasources-create-page/datasources-create-page.component"
    ).then((module) => ({ default: module.DatasourcesCreatePage }))
);

const DatasourcesUpdatePage = lazy(() =>
    import(
        /* webpackChunkName: "datasources-update-page" */ "../../pages/datasources-update-page/datasources-update-page.component"
    ).then((module) => ({ default: module.DatasourcesUpdatePage }))
);

const PageNotFoundPage = lazy(() =>
    import(
        /* webpackChunkName: "page-not-found-page" */ "../../pages/page-not-found-page/page-not-found-page.component"
    ).then((module) => ({ default: module.PageNotFoundPage }))
);

export const DatasourcesRouter: FunctionComponent = () => {
    const [loading, setLoading] = useState(true);
    const { setRouterBreadcrumbs } = useAppBreadcrumbs();
    const history = useHistory();
    const { t } = useTranslation();

    useEffect(() => {
        setRouterBreadcrumbs([
            {
                text: t("label.datasources"),
                onClick: () => history.push(getDatasourcesPath()),
            },
        ]);
        setLoading(false);
    }, []);

    if (loading) {
        return <AppLoadingIndicatorV1 />;
    }

    return (
        <Suspense fallback={<AppLoadingIndicatorV1 />}>
            <Switch>
                {/* Datasources path */}
                <Route exact path={AppRoute.DATASOURCES}>
                    {/* Redirect to datasources all path */}
                    <Redirect to={getDatasourcesAllPath()} />
                </Route>

                {/* Datasources all path */}
                <Route
                    exact
                    component={DatasourcesAllPage}
                    path={AppRoute.DATASOURCES_ALL}
                />

                {/* Datasources view path */}
                <Route
                    exact
                    component={DatasourcesViewPage}
                    path={AppRoute.DATASOURCES_VIEW}
                />

                {/* Datasources create path */}
                <Route
                    exact
                    component={DatasourcesCreatePage}
                    path={AppRoute.DATASOURCES_CREATE}
                />

                {/* Datasources update path */}
                <Route
                    exact
                    component={DatasourcesUpdatePage}
                    path={AppRoute.DATASOURCES_UPDATE}
                />

                {/* No match found, render page not found */}
                <Route component={PageNotFoundPage} />
            </Switch>
        </Suspense>
    );
};
