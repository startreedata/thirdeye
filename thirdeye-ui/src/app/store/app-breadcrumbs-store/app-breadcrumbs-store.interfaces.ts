import { Breadcrumb } from "../../components/app-breadcrumbs/app-breadcrumbs.interfaces";

export type AppBreadcrumbsStore = {
    appSectionBreadcrumbs: Breadcrumb[];
    pageBreadcrumbs: Breadcrumb[];
    appBreadcrumbs: Breadcrumb[];
    setAppSectionBreadcrumbs: (breadcrumbs: Breadcrumb[]) => void;
    setPageBreadcrumbs: (breadcrumbs: Breadcrumb[]) => void;
};
