import { Toolbar } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useAppBreadcrumbsStore } from "../../store/app-breadcrumbs-store/app-breadcrumbs-store";
import { AppBreadcrumbs } from "../app-breadcrumbs/app-breadcrumbs.component";
import { PageContainerProps } from "./page-container.interfaces";
import { usePageContainerStyles } from "./page-container.styles";

export const PageContainer: FunctionComponent<PageContainerProps> = (
    props: PageContainerProps
) => {
    const pageContainerClasses = usePageContainerStyles();
    const [appBreadcrumbs] = useAppBreadcrumbsStore((state) => [
        state.appBreadcrumbs,
    ]);

    return (
        <div className={pageContainerClasses.outerContainer}>
            {/* Required to clip the subsequent container under ApplicationBar */}
            <Toolbar />

            {!props.hideApplicaionBreadCrumbs && (
                // App breadcrumbs
                <AppBreadcrumbs breadcrumbs={appBreadcrumbs} />
            )}

            <div className={pageContainerClasses.innerContainer}>
                {/* Include children */}
                {props.children}
            </div>
        </div>
    );
};
