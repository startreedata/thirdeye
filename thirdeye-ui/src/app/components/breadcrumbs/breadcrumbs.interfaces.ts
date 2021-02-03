import { Variant } from "@material-ui/core/styles/createTypography";

export interface BreadcrumbsProps {
    breadcrumbs: Breadcrumb[];
    variant?: Variant;
    trailingSeparator?: boolean;
    maxItems?: number;
}

export interface Breadcrumb {
    text: string;
    onClick?: () => void;
}
