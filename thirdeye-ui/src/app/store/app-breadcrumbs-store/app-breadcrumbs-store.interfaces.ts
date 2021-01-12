import { Breadcrumb } from "../../components/breadcrumbs/breadcrumbs.interfaces";

export type AppBreadcrumbsStore = {
    routerBreadcrumbs: Breadcrumb[];
    pageBreadcrumbs: Breadcrumb[];
    setRouterBreadcrumbs: (breadcrumbs: Breadcrumb[]) => void;
    setPageBreadcrumbs: (breadcrumbs: Breadcrumb[]) => void;
    pushPageBreadcrumb: (breadcrumb: Breadcrumb) => void;
    popPageBreadcrumb: () => void;
};
