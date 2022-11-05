import { ReactNode } from "react";

export interface NavBarV1Props {
    homePath?: string;
    minimizeLabel: string;
    maximizeLabel: string;
    className?: string;
    children?: ReactNode;
}

export interface NavBarV1ContextProps {
    navBarMinimized: boolean;
}

export enum NavBarUserPreferenceV1 {
    Minimized,
    Maximized,
}
