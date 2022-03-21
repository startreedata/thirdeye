import create from "zustand";
import { AppBreadcrumbsStore } from "./app-breadcrumbs.interfaces";

// App store for app breadcrumbs
export const useAppBreadcrumbsStore = create<AppBreadcrumbsStore>(
    (set, get) => ({
        routerBreadcrumbs: [],
        pageBreadcrumbs: [],

        setRouterBreadcrumbs: (breadcrumbs) => {
            if (!breadcrumbs) {
                return;
            }

            set({
                routerBreadcrumbs: breadcrumbs,
            });
        },

        setPageBreadcrumbs: (breadcrumbs) => {
            if (!breadcrumbs) {
                return;
            }

            set({
                pageBreadcrumbs: breadcrumbs,
            });
        },

        pushPageBreadcrumb: (breadcrumb) => {
            if (!breadcrumb) {
                return;
            }

            const { pageBreadcrumbs } = get();
            set({
                pageBreadcrumbs: [...pageBreadcrumbs, breadcrumb],
            });
        },

        popPageBreadcrumb: () => {
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
