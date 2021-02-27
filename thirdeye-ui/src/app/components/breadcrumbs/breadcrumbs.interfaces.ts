import { Variant } from "@material-ui/core/styles/createTypography";

export interface BreadcrumbsProps {
    breadcrumbs: Breadcrumb[];
    variant?: Variant;
    breadcrumbMaxWidth?: number;
    maxItems?: number;
    trailingSeparator?: boolean;
}

export interface Breadcrumb {
    text: string;
    onClick?: () => void;
}
