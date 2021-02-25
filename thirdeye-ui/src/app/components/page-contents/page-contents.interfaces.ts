import { WithWidth } from "@material-ui/core";
import { ReactNode } from "react";

export interface PageContentsProps extends WithWidth {
    title: string;
    hideHeader?: boolean;
    hideTimeRange?: boolean;
    hideAppBreadcrumbs?: boolean;
    maxRouterBreadcrumbs?: number; // Maximum number of router breadcrumbs to display
    centered?: boolean;
    children?: ReactNode;
}
