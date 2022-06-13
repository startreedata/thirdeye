import { ReactNode } from "react";

export interface NameValueDisplayCardProps<T> {
    name: string;
    values: T[];
    link?: boolean;
    wrap?: boolean;
    showCount?: boolean;
    searchWords?: string[];
    valueClassName?: string;
    valueRenderer?: (value: T) => ReactNode;
    onClick?: (value: T) => void;
}
