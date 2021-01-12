import { Breadcrumb } from "../breadcrumbs/breadcrumbs.interfaces";

export interface UseAppBreadcrumbsProps {
    setRouterBreadcrumbs: (breadcrumbs: Breadcrumb[]) => void;
    setPageBreadcrumbs: (breadcrumbs: Breadcrumb[]) => void;
    pushPageBreadcrumb: (breadcrumb: Breadcrumb) => void;
    popPageBreadcrumb: () => void;
}
