import { Toolbar } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { PageContainerProps } from "./page-container.interfaces";
import { usePageContainerStyles } from "./page-container.styles";

export const PageContainer: FunctionComponent<PageContainerProps> = (
    props: PageContainerProps
) => {
    const pageContainerClasses = usePageContainerStyles();

    return (
        <div className={pageContainerClasses.pageContainer}>
            {/* Required to clip the container under app bar */}
            <Toolbar />

            {/* Contents */}
            <div className={pageContainerClasses.pageContainerContents}>
                {props.children}
            </div>
        </div>
    );
};
