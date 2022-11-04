import { ReactNode } from "react";

export interface AppContainerV1Props {
    name: string; // Application name, will show up in document title
    className?: string;
    children?: ReactNode;
}

export interface AppContainerV1ContextProps {
    name: string;
}
