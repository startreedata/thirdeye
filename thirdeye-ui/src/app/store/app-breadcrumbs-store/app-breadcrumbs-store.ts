import create, { GetState, SetState } from "zustand";
import { Breadcrumb } from "../../components/app-breadcrumbs/app-breadcrumbs.interfaces";
import { AppBreadcrumbsStore } from "./app-breadcrumbs-store.interfaces";

// App store for app breadcrumbs
export const useAppBreadcrumbsStore = create<AppBreadcrumbsStore>(
    (
        set: SetState<AppBreadcrumbsStore>,
        get: GetState<AppBreadcrumbsStore>
    ) => ({
        appBreadcrumbs: [],
        appSectionBreadcrumbs: [],
        pageBreadcrumbs: [],

        setAppSectionBreadcrumbs: (breadcrumbs: Breadcrumb[]): void => {
            const { pageBreadcrumbs } = get();

            set({
                appSectionBreadcrumbs: breadcrumbs,
                appBreadcrumbs: [...breadcrumbs, ...pageBreadcrumbs],
            });
        },

        setPageBreadcrumbs: (breadcrumbs: Breadcrumb[]): void => {
            const { appSectionBreadcrumbs } = get();

            set({
                pageBreadcrumbs: breadcrumbs,
                appBreadcrumbs: [...appSectionBreadcrumbs, ...breadcrumbs],
            });
        },
    })
);
