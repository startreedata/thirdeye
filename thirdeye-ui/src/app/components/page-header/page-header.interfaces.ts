import { ReactNode } from "react";
import { Crumb } from "../breadcrumbs/breadcrumbs.interfaces";

export interface PageHeaderProps {
    title?: string;
    subtitle?: ReactNode;
    showTimeRange?: boolean;
    showCreateButton?: boolean;
    transparentBackground?: boolean;
    breadcrumbs?: Crumb[];
    children?: ReactNode;
    customActions?: ReactNode;
    subNavigation?: SubNavigationConfiguration[];
    subNavigationSelected?: number;
}

export interface SubNavigationConfiguration extends Crumb {
    link: string;
}
