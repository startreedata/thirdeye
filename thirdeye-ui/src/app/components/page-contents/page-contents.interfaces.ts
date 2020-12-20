import { ReactNode } from "react";

export interface PageContentsProps {
    title?: string;
    titleCenterAlign?: boolean;
    hideTimeRange?: boolean;
    contentsCenterAlign?: boolean;
    children?: ReactNode;
}
