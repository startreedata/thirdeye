import create, { SetState } from "zustand";
import {
    ApplicationBreadcrumbsStore,
    Breadcrumb,
} from "./application-breadcrumbs-store.interfaces";

// Application store for application breadcrumbs
export const useApplicationBreadcrumbsStore = create<
    ApplicationBreadcrumbsStore
>((set: SetState<ApplicationBreadcrumbsStore>) => ({
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
}));
