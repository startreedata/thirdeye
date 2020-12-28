import { ReactNode } from "react";

export interface PageContentsProps {
    title?: string;
    hideTimeRange?: boolean;
    centered?: boolean;
    children?: ReactNode;
}
