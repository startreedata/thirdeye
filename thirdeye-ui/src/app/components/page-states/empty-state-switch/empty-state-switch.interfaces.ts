import { ReactNode } from "react";

export interface EmptyStateSwitchProps {
    children: ReactNode;
    emptyState: ReactNode;
    isEmpty: boolean;
}
