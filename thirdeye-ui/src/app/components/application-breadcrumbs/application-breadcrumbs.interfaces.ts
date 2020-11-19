export interface Breadcrumb {
    text: string;
    path: string;
}

export interface ApplicationBreadcrumbsProps {
    breadcrumbs: Breadcrumb[];
}
