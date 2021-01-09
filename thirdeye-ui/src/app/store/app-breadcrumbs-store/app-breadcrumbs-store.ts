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
            if (!breadcrumbs) {
                return;
            }

            const { pageBreadcrumbs } = get();
            set({
                appBreadcrumbs: [...breadcrumbs, ...pageBreadcrumbs],
                appSectionBreadcrumbs: breadcrumbs,
            });
        },

        setPageBreadcrumbs: (breadcrumbs: Breadcrumb[]): void => {
            if (!breadcrumbs) {
                return;
            }

            const { appSectionBreadcrumbs } = get();
            set({
                appBreadcrumbs: [...appSectionBreadcrumbs, ...breadcrumbs],
                pageBreadcrumbs: breadcrumbs,
            });
        },

        pushPageBreadcrumb: (breadcrumb: Breadcrumb): void => {
            if (!breadcrumb) {
                return;
            }

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
            const newPageBreadcrumbs = pageBreadcrumbs.slice(
                0,
                pageBreadcrumbs.length - 1
            );
            set({
                appBreadcrumbs: [
                    ...appSectionBreadcrumbs,
                    ...newPageBreadcrumbs,
                ],
                pageBreadcrumbs: [...newPageBreadcrumbs],
            });
        },
    })
);
