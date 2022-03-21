import { ReactNode } from "react";
import { Breadcrumb } from "../../breadcrumbs/breadcrumbs.interfaces";

export interface AppBreadcrumbsProviderProps {
    children: ReactNode;
}

export interface UseAppBreadcrumbsProps {
    setRouterBreadcrumbs: (breadcrumbs: Breadcrumb[]) => void;
    setPageBreadcrumbs: (breadcrumbs: Breadcrumb[]) => void;
    pushPageBreadcrumb: (breadcrumb: Breadcrumb) => void;
    popPageBreadcrumb: () => void;
}

export interface AppBreadcrumbsContextProps extends UseAppBreadcrumbsProps {
    routerBreadcrumbs: Breadcrumb[];
    pageBreadcrumbs: Breadcrumb[];
}
