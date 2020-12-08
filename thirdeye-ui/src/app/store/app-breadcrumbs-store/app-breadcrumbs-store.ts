import { isEmpty } from "lodash";
import create, { GetState, SetState } from "zustand";
import { Breadcrumb } from "../../components/app-breadcrumbs/app-breadcrumbs.interfaces";
import { AppBreadcrumbsStore } from "./app-breadcrumbs-store.interfaces";

// App store for app breadcrumbs
export const useAppBreadcrumbsStore = create<AppBreadcrumbsStore>(
    (
        set: SetState<AppBreadcrumbsStore>,
        get: GetState<AppBreadcrumbsStore>
    ) => ({
        appSectionBreadcrumb: {} as Breadcrumb,
        pageBreadcrumbs: [],
        appBreadcrumbs: [],

        setAppSectionBreadcrumb: (breadcrumb: Breadcrumb): void => {
            const { pageBreadcrumbs } = get();

            set({
                // Set appSectionBreadcrumb
                appSectionBreadcrumb: breadcrumb,
                // Create app breadcrumbs using existing pageBreadcrumbs and new
                // appSectionBreadcrumb depending on whether it's empty
                appBreadcrumbs: isEmpty(breadcrumb)
                    ? pageBreadcrumbs
                    : [breadcrumb, ...pageBreadcrumbs],
            });
        },

        setPageBreadcrumbs: (breadcrumbs: Breadcrumb[]): void => {
            const { appSectionBreadcrumb } = get();

            set({
                // Set pageBreadcrumbs
                pageBreadcrumbs: breadcrumbs,
                // Create app breadcrumbs using new pageBreadcrumbs and existing
                // appSectionBreadcrumb depending on whether it's empty
                appBreadcrumbs: isEmpty(appSectionBreadcrumb)
                    ? breadcrumbs
                    : [appSectionBreadcrumb, ...breadcrumbs],
            });
        },

        pushPageBreadcrumb: (breadcrumb: Breadcrumb): void => {
            const { pageBreadcrumbs, appSectionBreadcrumb } = get();

            set({
                // Set pageBreadcrumbs
                pageBreadcrumbs: [...pageBreadcrumbs, breadcrumb],
                // Create app breadcrumbs using new pageBreadcrumbs and existing
                // appSectionBreadcrumb depending on whether it's empty
                appBreadcrumbs: isEmpty(appSectionBreadcrumb)
                    ? [...pageBreadcrumbs, breadcrumb]
                    : [appSectionBreadcrumb, ...pageBreadcrumbs, breadcrumb],
            });
        },
    })
);
