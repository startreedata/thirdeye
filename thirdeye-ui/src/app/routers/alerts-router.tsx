import React, { FunctionComponent } from "react";
import { Redirect, Route, Switch } from "react-router-dom";
import { CreateAlertPage } from "../pages/alert-create-page/create-alert.component";
import { AlertsAllPage } from "../pages/alerts-all-page/alerts-all-page.component";
import { AlertsDetailPage } from "../pages/alerts-detail-page/alerts-detail-page.component";
import { AppRoute, getAlertsAllPath } from "../utils/routes.util";

// ThirdEye UI alerts path router
export const AlertsRouter: FunctionComponent = () => {
    return (
        <Switch>
            <Route
                exact
                component={CreateAlertPage}
                path={AppRoute.ALERT_NEW}
            />
            {/* Alerts path */}
            <Route exact path={AppRoute.ALERTS}>
                {/* Redirect to alerts - all path */}
                <Redirect to={getAlertsAllPath()} />
            </Route>

            {/* Alerts - all path */}
            <Route exact component={AlertsAllPage} path={AppRoute.ALERTS_ALL} />
            <Route
                exact
                component={AlertsDetailPage}
                path={AppRoute.ALERTS_DETAIL}
            />
            <Route
                exact
                component={AlertsAllPage}
                path={AppRoute.ALERTS_EDIT}
            />
        </Switch>
    );
};
