export interface Breadcrumb {
    text: string;
    path: string;
}

export interface AppBreadcrumbsProps {
    breadcrumbs: Breadcrumb[];
}
