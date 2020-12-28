import { ReactNode } from "react";
import create, { SetState } from "zustand";
import { AppToolbarStore } from "./app-toolbar-store.interfaces";

// App store for app toolbar
export const useAppToolbarStore = create<AppToolbarStore>(
    (set: SetState<AppToolbarStore>) => ({
        appToolbar: null,

        setAppToolbar: (appToolbar: ReactNode): void => {
            set({
                appToolbar: appToolbar,
            });
        },

        removeAppToolbar: (): void => {
            set({
                appToolbar: null,
            });
        },
    })
);
