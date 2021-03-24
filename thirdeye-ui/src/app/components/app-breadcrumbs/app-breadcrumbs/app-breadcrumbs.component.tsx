import { isNil } from "lodash";
import React, { FunctionComponent, useContext } from "react";
import { Breadcrumbs } from "../../breadcrumbs/breadcrumbs.component";
import { AppBreadcrumbsContext } from "../app-breadcrumbs-provider/app-breadcrumbs-provider.component";
import { AppBreadcrumbsProps } from "./app-breadcrumbs.interfaces";

const MAX_WIDTH_APP_BREADCRUMB = 120;
const MAX_ITEMS_APP_BREADCRUMBS = 4;

export const AppBreadcrumbs: FunctionComponent<AppBreadcrumbsProps> = (
    props: AppBreadcrumbsProps
) => {
    const { routerBreadcrumbs, pageBreadcrumbs } = useContext(
        AppBreadcrumbsContext
    );

    return (
        <Breadcrumbs
            trailingSeparator
            breadcrumbMaxWidth={MAX_WIDTH_APP_BREADCRUMB}
            breadcrumbs={
                !isNil(props.maxRouterBreadcrumbs)
                    ? [
                          ...routerBreadcrumbs.slice(
                              0,
                              props.maxRouterBreadcrumbs
                          ),
                          ...pageBreadcrumbs,
                      ]
                    : [...routerBreadcrumbs, ...pageBreadcrumbs]
            }
            maxItems={MAX_ITEMS_APP_BREADCRUMBS}
        />
    );
};
