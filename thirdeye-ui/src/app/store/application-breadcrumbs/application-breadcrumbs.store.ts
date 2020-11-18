import create, { GetState, SetState } from "zustand";
import {
    ApplicationBreadcrumbs,
    Breadcrumb,
} from "./application-breadcrumbs.interfaces";

export const useApplicationBreadcrumbsStore = create<ApplicationBreadcrumbs>(
    (
        set: SetState<ApplicationBreadcrumbs>,
        get: GetState<ApplicationBreadcrumbs>
    ) => ({
        breadcrumbs: [],

        push: (breadcrumbs: Breadcrumb[], resetBeforePush?: boolean): void => {
            // Get current application breadcrumbs
            const { breadcrumbs: currentBreadcrumbs } = get();

            if (resetBeforePush) {
                // Clear breadcrumbs and add new
                set({ breadcrumbs: breadcrumbs });
            } else {
                // Add new
                set({ breadcrumbs: currentBreadcrumbs.concat(breadcrumbs) });
            }
        },
    })
);
