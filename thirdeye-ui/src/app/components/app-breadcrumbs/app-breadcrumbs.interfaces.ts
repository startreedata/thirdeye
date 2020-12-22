export interface AppBreadcrumbsProps {
    breadcrumbs: Breadcrumb[];
}
export interface Breadcrumb {
    text: string;
    pathFn?: () => string; // Function that shall return path to be routed to
}
