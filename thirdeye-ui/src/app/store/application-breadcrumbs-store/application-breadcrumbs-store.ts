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
        appSectionBreadcrumb: {} as Breadcrumb,
        pageBreadcrumbs: [],
        applcationBreadcrumbs: [],

        setAppSectionBreadcrumb: (breadcrumb: Breadcrumb): void => {
            const { pageBreadcrumbs } = get();

            set({
                // Set appSectionBreadcrumb
                appSectionBreadcrumb: breadcrumb,
                // Create application breadcrumbs using existing pageBreadcrumbs and new
                // appSectionBreadcrumb depending on whether it's empty
                applcationBreadcrumbs: isEmpty(breadcrumb)
                    ? pageBreadcrumbs
                    : [breadcrumb, ...pageBreadcrumbs],
            });
        },

        setPageBreadcrumbs: (breadcrumbs: Breadcrumb[]): void => {
            const { appSectionBreadcrumb } = get();

            set({
                // Set pageBreadcrumbs
                pageBreadcrumbs: breadcrumbs,
                // Create application breadcrumbs using new pageBreadcrumbs and existing
                // appSectionBreadcrumb depending on whether it's empty
                applcationBreadcrumbs: isEmpty(appSectionBreadcrumb)
                    ? breadcrumbs
                    : [appSectionBreadcrumb, ...breadcrumbs],
            });
        },
    })
);
