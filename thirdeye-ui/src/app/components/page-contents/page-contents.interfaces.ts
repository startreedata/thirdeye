import { ReactNode } from "react";

export interface PageContentsProps {
    title?: string;
    titleCenterAlign?: boolean;
    hideTimeRange?: boolean;
    centerAlign?: boolean;
    children?: ReactNode;
}
