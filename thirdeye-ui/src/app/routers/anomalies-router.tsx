import React, { FunctionComponent } from "react";
import { Redirect, Route, Switch } from "react-router-dom";
import { AnomaliesAllPage } from "../pages/anomalies-all-page/anomalies-all-page.component";
import { AnomaliesDetailPage } from "../pages/anomalies-detail-page/anomalies-detail-page.component";
import {
    AppRoute,
    getAnomaliesAllPath,
    getPageNotFoundPath,
} from "../utils/route/routes.util";

export const AnomaliesRouter: FunctionComponent = () => {
    return (
        <Switch>
            {/* Anomalies path */}
            <Route exact path={AppRoute.ANOMALIES}>
                {/* Redirect to anomalies all path */}
                <Redirect to={getAnomaliesAllPath()} />
            </Route>

            {/* Anomalies all path */}
            <Route
                exact
                component={AnomaliesAllPage}
                path={AppRoute.ANOMALIES_ALL}
            />

            {/* Anomalies detail path */}
            <Route
                exact
                component={AnomaliesDetailPage}
                path={AppRoute.ANOMALIES_DETAIL}
            />

            {/* No match found, redirect to page not found path */}
            <Redirect to={getPageNotFoundPath()} />
        </Switch>
    );
};
