import create, { SetState } from "zustand";
import { RedirectionPathStore } from "./redirection-path-store.interfaces";

// Application store for redirection path, particularly to redirect after signing in
export const useRedirectionPathStore = create<RedirectionPathStore>(
    (set: SetState<RedirectionPathStore>) => ({
        redirectToPath: "",

        setRedirectToPath: (redirectToPath: string): void => {
            set({
                redirectToPath: redirectToPath,
            });
        },

        clearRedirectToPath: (): void => {
            set({
                redirectToPath: "",
            });
        },
    })
);
