import { ReactNode } from "react";

export interface NameValueDisplayCardProps<T> {
    name: string;
    values: T[];
    showCount?: boolean;
    link?: boolean;
    wrap?: boolean;
    searchWords?: string[];
    valueClassName?: string;
    valueRenderer?: (value: T) => ReactNode;
    onClick?: (value: T) => void;
}
