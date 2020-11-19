import { Toolbar } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { ApplicationBreadcrumbs } from "../application-breadcrumbs/application-breadcrumbs.component";
import { PageContainerProps } from "./page-container.interfaces";
import { pageContainerStyles } from "./page-container.styles";

export const PageContainer: FunctionComponent<PageContainerProps> = (
    props: PageContainerProps
) => {
    const pageContainerClasses = pageContainerStyles();

    return (
        <main className={pageContainerClasses.main}>
            {/* Required to clip the subsequent container under ApplicationBar */}
            <Toolbar />

            {/* Application breadcrumbs */}
            <ApplicationBreadcrumbs />

            <div className={pageContainerClasses.container}>
                {/* Include children */}
                {props.children}
            </div>
        </main>
    );
};
