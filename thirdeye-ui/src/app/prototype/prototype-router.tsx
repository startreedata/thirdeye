import React, { FunctionComponent, useEffect } from "react";
import { Route, Switch } from "react-router-dom";
import { useAppBreadcrumbsStore } from "../store/app-breadcrumbs-store/app-breadcrumbs-store";
import { useAppToolbarStore } from "../store/app-toolbar-store/app-toolbar-store";
import { AppToolbarPrototype } from "./app-toolbar-prototype.component";
import { PrototypeEntityDetailPage } from "./prototype-entity-detail-page.component";
import { PrototypeEntityListPage } from "./prototype-entity-list-page.component";

export const PrototypeRouter: FunctionComponent = () => {
    const [setAppSectionBreadcrumbs] = useAppBreadcrumbsStore((state) => [
        state.setAppSectionBreadcrumbs,
    ]);
    const [setAppToolbar] = useAppToolbarStore((state) => [
        state.setAppToolbar,
    ]);

    useEffect(() => {
        setAppSectionBreadcrumbs([
            {
                text: "PROTOTYPE",
            },
        ]);

        setAppToolbar(<AppToolbarPrototype />);
    }, []);

    return (
        <Switch>
            <Route
                exact
                component={PrototypeEntityListPage}
                path={"/prototype/entity/list"}
            />

            <Route
                exact
                component={PrototypeEntityDetailPage}
                path={"/prototype/entity/detail"}
            />
        </Switch>
    );
};
