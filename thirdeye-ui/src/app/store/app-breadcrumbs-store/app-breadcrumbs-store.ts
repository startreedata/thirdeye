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
                appBreadcrumbs: [...breadcrumbs, ...pageBreadcrumbs],
                appSectionBreadcrumbs: breadcrumbs,
            });
        },

        setPageBreadcrumbs: (breadcrumbs: Breadcrumb[]): void => {
            const { appSectionBreadcrumbs } = get();

            set({
                appBreadcrumbs: [...appSectionBreadcrumbs, ...breadcrumbs],
                pageBreadcrumbs: breadcrumbs,
            });
        },

        pushPageBreadcrumb: (breadcrumb: Breadcrumb): void => {
            const { appSectionBreadcrumbs, pageBreadcrumbs } = get();

            set({
                appBreadcrumbs: [
                    ...appSectionBreadcrumbs,
                    ...pageBreadcrumbs,
                    breadcrumb,
                ],
                pageBreadcrumbs: [...pageBreadcrumbs, breadcrumb],
            });
        },

        popPageBreadcrumb: (): void => {
            const { appSectionBreadcrumbs, pageBreadcrumbs } = get();
            pageBreadcrumbs.pop();

            set({
                appBreadcrumbs: [...appSectionBreadcrumbs, ...pageBreadcrumbs],
                pageBreadcrumbs: [...pageBreadcrumbs],
            });
        },
    })
);
