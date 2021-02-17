import { ReactNode } from "react";

export interface TooltipWithBoundsProps {
    top?: number;
    left?: number;
    open?: boolean;
    title: ReactNode;
    children: ReactNode;
}
