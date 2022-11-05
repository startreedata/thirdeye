import { ReactNode } from "react";

export interface BreadcrumbsProps {
    crumbs: Crumb[];
}

export interface Crumb {
    label: ReactNode;
    link?: string;
}
