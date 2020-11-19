export interface Breadcrumb {
    text: string;
    path: string;
}

export type ApplicationBreadcrumbs = {
    routerBreadcrumb: Breadcrumb;
    pageBreadcrumbs: Breadcrumb[];
    setRouterBreadcrumb: (breadcrumb: Breadcrumb) => void;
    setPageBreadcrumbs: (breadcrumb: Breadcrumb[]) => void;
};
