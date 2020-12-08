import { Breadcrumb } from "../../components/app-breadcrumbs/app-breadcrumbs.interfaces";

export type AppBreadcrumbsStore = {
    appSectionBreadcrumb: Breadcrumb;
    pageBreadcrumbs: Breadcrumb[];
    appBreadcrumbs: Breadcrumb[];
    setAppSectionBreadcrumb: (breadcrumb: Breadcrumb) => void;
    setPageBreadcrumbs: (breadcrumbs: Breadcrumb[]) => void;
    pushPageBreadcrumb: (breadcrumb: Breadcrumb) => void;
};
