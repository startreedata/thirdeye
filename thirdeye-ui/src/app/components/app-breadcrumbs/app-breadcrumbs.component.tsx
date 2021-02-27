import React, { FunctionComponent } from "react";
import { useAppBreadcrumbsStore } from "../../stores/app-breadcrumbs/app-breadcrumbs.store";
import { Breadcrumbs } from "../breadcrumbs/breadcrumbs.component";
import {
    AppBreadcrumbsProps,
    UseAppBreadcrumbsProps,
} from "./app-breadcrumbs.interfaces";

const MAX_WIDTH_APP_BREADCRUMB = 120;
const MAX_ITEMS_APP_BREADCRUMBS = 4;

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
            breadcrumbMaxWidth={MAX_WIDTH_APP_BREADCRUMB}
            breadcrumbs={[
                ...(props.maxRouterBreadcrumbs
                    ? routerBreadcrumbs.slice(0, props.maxRouterBreadcrumbs)
                    : routerBreadcrumbs),
                ...pageBreadcrumbs,
            ]}
            maxItems={MAX_ITEMS_APP_BREADCRUMBS}
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
