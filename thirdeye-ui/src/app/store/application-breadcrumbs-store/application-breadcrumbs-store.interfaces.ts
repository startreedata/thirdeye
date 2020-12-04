import { Breadcrumb } from "../../components/application-breadcrumbs/application-breadcrumbs.interfaces";

export type ApplicationBreadcrumbsStore = {
    appSectionBreadcrumb: Breadcrumb;
    pageBreadcrumbs: Breadcrumb[];
    applcationBreadcrumbs: Breadcrumb[];
    setAppSectionBreadcrumb: (breadcrumb: Breadcrumb) => void;
    setPageBreadcrumbs: (breadcrumbs: Breadcrumb[]) => void;
};
