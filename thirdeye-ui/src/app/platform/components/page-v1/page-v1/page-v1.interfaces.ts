import { ReactNode } from "react";

export interface PageV1Props {
    className?: string;
    children?: ReactNode;
}

export interface PageV1ContextProps {
    headerVisible: boolean;
    setHeaderVisible: (headerVisible: boolean) => void;
    setHeaderText: (headerText: ReactNode) => void;
    setCurrentHeaderTab: (currentHeaderTab: ReactNode) => void;
}
