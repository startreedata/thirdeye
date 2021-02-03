import React, { FunctionComponent } from "react";
import { useAppBreadcrumbsStore } from "../../stores/app-breadcrumbs-store/app-breadcrumbs-store";
import { Breadcrumbs } from "../breadcrumbs/breadcrumbs.component";
import {
    AppBreadcrumbsProps,
    UseAppBreadcrumbsProps,
} from "./app-breadcrumbs.interfaces";

export const AppBreadcrumbs: FunctionComponent<AppBreadcrumbsProps> = (
    props: AppBreadcrumbsProps
) => {
    const [
        routerBreadcrumbs,
        pageBreadcrumbs,
    ] = useAppBreadcrumbsStore((state) => [
        state.routerBreadcrumbs,
        state.pageBreadcrumbs,
    ]);

    return (
        <Breadcrumbs
            trailingSeparator
            breadcrumbs={
                props.maxRouterBreadcrumbs
                    ? [
                          ...routerBreadcrumbs.slice(
                              0,
                              props.maxRouterBreadcrumbs
                          ),
                          ...pageBreadcrumbs,
                      ]
                    : [...routerBreadcrumbs, ...pageBreadcrumbs]
            }
            maxItems={4}
            variant="body2"
        />
    );
};

export const useAppBreadcrumbs = (): UseAppBreadcrumbsProps => {
    return useAppBreadcrumbsStore((state) => ({
        routerBreadcrumbs: state.routerBreadcrumbs,
        pageBreadcrumbs: state.pageBreadcrumbs,
        setRouterBreadcrumbs: state.setRouterBreadcrumbs,
        setPageBreadcrumbs: state.setPageBreadcrumbs,
        pushPageBreadcrumb: state.pushPageBreadcrumb,
        popPageBreadcrumb: state.popPageBreadcrumb,
    }));
};
