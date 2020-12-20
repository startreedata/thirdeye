import { Breadcrumb } from "../../components/app-breadcrumbs/app-breadcrumbs.interfaces";

export type AppBreadcrumbsStore = {
    appBreadcrumbs: Breadcrumb[];
    appSectionBreadcrumbs: Breadcrumb[];
    pageBreadcrumbs: Breadcrumb[];
    setAppSectionBreadcrumbs: (breadcrumbs: Breadcrumb[]) => void;
    setPageBreadcrumbs: (breadcrumbs: Breadcrumb[]) => void;
};
