import { ReactNode } from "react";

export interface AppDrawerProps {
    open?: boolean;
    children?: ReactNode;
    onChange?: (open: boolean) => void;
}
