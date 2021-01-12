export interface BreadcrumbsProps {
    breadcrumbs: Breadcrumb[];
}

export interface Breadcrumb {
    text: string;
    onClick?: () => void;
}
