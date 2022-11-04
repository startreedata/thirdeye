import { ReactNode } from "react";

export interface PageHeaderTabV1Props {
    href: string;
    selected?: boolean;
    value?: number | string;
    disabled?: boolean;
    className?: string;
    children?: ReactNode;
}
