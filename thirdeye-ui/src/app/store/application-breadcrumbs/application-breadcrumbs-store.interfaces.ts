import { Breadcrumb } from "../../components/application-breadcrumbs/application-breadcrumbs.interfaces";

export type ApplicationBreadcrumbsStore = {
    routerBreadcrumb: Breadcrumb;
    pageBreadcrumbs: Breadcrumb[];
    breadcrumbs: Breadcrumb[];
    setRouterBreadcrumb: (breadcrumb: Breadcrumb) => void;
    setPageBreadcrumbs: (breadcrumbs: Breadcrumb[]) => void;
};
