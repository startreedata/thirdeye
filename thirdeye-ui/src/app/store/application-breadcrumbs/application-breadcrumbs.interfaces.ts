export interface Breadcrumb {
    text: string;
    path: string;
}

export type ApplicationBreadcrumbs = {
    breadcrumbs: Breadcrumb[];
    push: (breadcrumbs: Breadcrumb[], resetBeforePush?: boolean) => void;
};
