import { Breadcrumb } from "../breadcrumbs/breadcrumbs.interfaces";

export interface AppBreadcrumbsProps {
    maxRouterBreadcrumbs?: number; // Maximum number of router breadcrumbs to render
}

export interface UseAppBreadcrumbsProps {
    routerBreadcrumbs: Breadcrumb[];
    pageBreadcrumbs: Breadcrumb[];
    setRouterBreadcrumbs: (breadcrumbs: Breadcrumb[]) => void;
    setPageBreadcrumbs: (breadcrumbs: Breadcrumb[]) => void;
    pushPageBreadcrumb: (breadcrumb: Breadcrumb) => void;
    popPageBreadcrumb: () => void;
}
