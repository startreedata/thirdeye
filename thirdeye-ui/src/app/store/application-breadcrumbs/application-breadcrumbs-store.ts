import { isEmpty } from "lodash";
import create, { GetState, SetState } from "zustand";
import { Breadcrumb } from "../../components/application-breadcrumbs/application-breadcrumbs.interfaces";
import { ApplicationBreadcrumbsStore } from "./application-breadcrumbs-store.interfaces";

// Application store for application breadcrumbs
export const useApplicationBreadcrumbsStore = create<
    ApplicationBreadcrumbsStore
>(
    (
        set: SetState<ApplicationBreadcrumbsStore>,
        get: GetState<ApplicationBreadcrumbsStore>
    ) => ({
        routerBreadcrumb: {} as Breadcrumb,
        pageBreadcrumbs: [],
        breadcrumbs: [],

        setRouterBreadcrumb: (breadcrumb: Breadcrumb): void => {
            const { pageBreadcrumbs } = get();

            set({
                // Set routerBreadcrumb
                routerBreadcrumb: breadcrumb,
                // Create application breadcrumbs using existing pageBreadcrumbs and new
                // routerBreadcrumb depending on whether it's empty
                breadcrumbs: isEmpty(breadcrumb)
                    ? pageBreadcrumbs
                    : [breadcrumb, ...pageBreadcrumbs],
            });
        },

        setPageBreadcrumbs: (breadcrumbs: Breadcrumb[]): void => {
            const { routerBreadcrumb } = get();

            set({
                // Set pageBreadcrumbs
                pageBreadcrumbs: breadcrumbs,
                // Create application breadcrumbs using new pageBreadcrumbs and existing
                // routerBreadcrumb depending on whether it's empty
                breadcrumbs: isEmpty(routerBreadcrumb)
                    ? breadcrumbs
                    : [routerBreadcrumb, ...breadcrumbs],
            });
        },
    })
);
