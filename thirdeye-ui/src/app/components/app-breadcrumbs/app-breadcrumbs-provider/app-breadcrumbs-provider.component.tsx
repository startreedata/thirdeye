import * as React from "react";
import { createContext, FunctionComponent, useContext } from "react";
import { useAppBreadcrumbsStore } from "../../../stores/app-breadcrumbs/app-breadcrumbs.store";
import {
    AppBreadcrumbsContextProps,
    AppBreadcrumbsProviderProps,
    UseAppBreadcrumbsProps,
} from "./app-breadcrumbs-provider.interfaces";

export const AppBreadcrumbsProvider: FunctionComponent<AppBreadcrumbsProviderProps> = (
    props: AppBreadcrumbsProviderProps
) => {
    const [
        routerBreadcrumbs,
        pageBreadcrumbs,
        setRouterBreadcrumbs,
        setPageBreadcrumbs,
        pushPageBreadcrumb,
        popPageBreadcrumb,
    ] = useAppBreadcrumbsStore((state) => [
        state.routerBreadcrumbs,
        state.pageBreadcrumbs,
        state.setRouterBreadcrumbs,
        state.setPageBreadcrumbs,
        state.pushPageBreadcrumb,
        state.popPageBreadcrumb,
    ]);

    const appBreadcrumbsContext: AppBreadcrumbsContextProps = {
        routerBreadcrumbs: routerBreadcrumbs,
        pageBreadcrumbs: pageBreadcrumbs,
        setRouterBreadcrumbs: setRouterBreadcrumbs,
        setPageBreadcrumbs: setPageBreadcrumbs,
        pushPageBreadcrumb: pushPageBreadcrumb,
        popPageBreadcrumb: popPageBreadcrumb,
    };

    return (
        <AppBreadcrumbsContext.Provider value={appBreadcrumbsContext}>
            {props.children}
        </AppBreadcrumbsContext.Provider>
    );
};

export const AppBreadcrumbsContext = createContext<AppBreadcrumbsContextProps>(
    {} as AppBreadcrumbsContextProps
);

export const useAppBreadcrumbs = (): UseAppBreadcrumbsProps => {
    return useContext(AppBreadcrumbsContext);
};
