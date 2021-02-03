import create, { GetState, SetState } from "zustand";
import { Breadcrumb } from "../../components/breadcrumbs/breadcrumbs.interfaces";
import { AppBreadcrumbsStore } from "./app-breadcrumbs-store.interfaces";

// App store for app breadcrumbs
export const useAppBreadcrumbsStore = create<AppBreadcrumbsStore>(
    (
        set: SetState<AppBreadcrumbsStore>,
        get: GetState<AppBreadcrumbsStore>
    ): AppBreadcrumbsStore => ({
        routerBreadcrumbs: [],
        pageBreadcrumbs: [],

        setRouterBreadcrumbs: (breadcrumbs: Breadcrumb[]): void => {
            if (!breadcrumbs) {
                return;
            }

            set({
                routerBreadcrumbs: breadcrumbs,
            });
        },

        setPageBreadcrumbs: (breadcrumbs: Breadcrumb[]): void => {
            if (!breadcrumbs) {
                return;
            }

            set({
                pageBreadcrumbs: breadcrumbs,
            });
        },

        pushPageBreadcrumb: (breadcrumb: Breadcrumb): void => {
            if (!breadcrumb) {
                return;
            }

            const { pageBreadcrumbs } = get();
            set({
                pageBreadcrumbs: [...pageBreadcrumbs, breadcrumb],
            });
        },

        popPageBreadcrumb: (): void => {
            const { pageBreadcrumbs } = get();
            set({
                pageBreadcrumbs: pageBreadcrumbs.slice(
                    0,
                    pageBreadcrumbs.length - 1
                ),
            });
        },
    })
);
