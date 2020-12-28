import { ReactNode } from "react";

export type AppToolbarStore = {
    appToolbar: ReactNode;
    setAppToolbar: (appToolbar: ReactNode) => void;
    removeAppToolbar: () => void;
};
