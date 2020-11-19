import { Toolbar } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useApplicationBreadcrumbsStore } from "../../store/application-breadcrumbs/application-breadcrumbs-store";
import { ApplicationBreadcrumbs } from "../application-breadcrumbs/application-breadcrumbs.component";
import { PageContainerProps } from "./page-container.interfaces";
import { usePageContainerStyles } from "./page-container.styles";

export const PageContainer: FunctionComponent<PageContainerProps> = (
    props: PageContainerProps
) => {
    const pageContainerClasses = usePageContainerStyles();
    const [breadcrumbs] = useApplicationBreadcrumbsStore((state) => [
        state.breadcrumbs,
    ]);

    return (
        <div className={pageContainerClasses.outerContainer}>
            {/* Required to clip the subsequent container under ApplicationBar */}
            <Toolbar />

            {!props.hideApplicaionBreadCrumbs && (
                // Application breadcrumbs
                <ApplicationBreadcrumbs breadcrumbs={breadcrumbs} />
            )}

            <div className={pageContainerClasses.innerContainer}>
                {/* Include children */}
                {props.children}
            </div>
        </div>
    );
};
