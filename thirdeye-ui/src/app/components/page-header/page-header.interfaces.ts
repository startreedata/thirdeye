import { ReactNode } from "react";

export interface PageHeaderProps {
    title: string;
    showTimeRange?: boolean;
    showCreateButton?: boolean;
    children?: ReactNode;
}
