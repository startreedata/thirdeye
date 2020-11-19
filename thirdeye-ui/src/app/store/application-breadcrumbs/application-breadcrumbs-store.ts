import create, { SetState } from "zustand";
import {
    ApplicationBreadcrumbs,
    Breadcrumb,
} from "./application-breadcrumbs-store.interfaces";

// Application store for application breadcrumbs
export const useApplicationBreadcrumbsStore = create<ApplicationBreadcrumbs>(
    (set: SetState<ApplicationBreadcrumbs>) => ({
        routerBreadcrumb: {} as Breadcrumb,
        pageBreadcrumbs: [],

        setRouterBreadcrumb: (breadcrumb: Breadcrumb): void => {
            set({
                routerBreadcrumb: breadcrumb,
            });
        },

        setPageBreadcrumbs: (breadcrumbs: Breadcrumb[]): void => {
            set({
                pageBreadcrumbs: breadcrumbs,
            });
        },
    })
);
