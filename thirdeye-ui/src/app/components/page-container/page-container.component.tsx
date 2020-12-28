import { Toolbar } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useAppBreadcrumbsStore } from "../../store/app-breadcrumbs-store/app-breadcrumbs-store";
import { useAppToolbarStore } from "../../store/app-toolbar-store/app-toolbar-store";
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
    const [appToolbar] = useAppToolbarStore((state) => [state.appToolbar]);

    return (
        <div className={pageContainerClasses.outerContainer}>
            {/* Required to clip the subsequent container under app bar */}
            <Toolbar />

            {/* App toolbar */}
            {appToolbar}

            {/* App breadcrumbs */}
            <AppBreadcrumbs breadcrumbs={appBreadcrumbs} />

            {/* Contents */}
            <div className={pageContainerClasses.innerContainer}>
                {props.children}
            </div>
        </div>
    );
};
