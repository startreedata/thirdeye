import React, { FunctionComponent } from "react";
import { Redirect, Route, Switch } from "react-router-dom";
import { HomePage } from "../pages/home-page/home-page.component";
import { AppRoute, getPageNotFoundPath } from "../utils/route/routes.util";

export const HomeRouter: FunctionComponent = () => {
    return (
        <Switch>
            {/* Home path */}
            <Route exact component={HomePage} path={AppRoute.HOME} />

            {/* No match found, redirect to page not found path */}
            <Redirect to={getPageNotFoundPath()} />
        </Switch>
    );
};
