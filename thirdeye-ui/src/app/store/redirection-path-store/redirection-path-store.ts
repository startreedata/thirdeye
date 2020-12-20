import create, { SetState } from "zustand";
import { RedirectionPathStore } from "./redirection-path-store.interfaces";

// App store for redirection path, particularly to redirect after signing in
export const useRedirectionPathStore = create<RedirectionPathStore>(
    (set: SetState<RedirectionPathStore>) => ({
        redirectionPath: "",

        setRedirectionPath: (path: string): void => {
            set({
                redirectionPath: path,
            });
        },

        clearRedirectionPath: (): void => {
            set({
                redirectionPath: "",
            });
        },
    })
);
