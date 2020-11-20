import { ReactNode } from "react";

export interface PageContentsProps {
    title?: string;
    hideTimeRange?: boolean;
    centerAlign?: boolean;
    children?: ReactNode;
}
