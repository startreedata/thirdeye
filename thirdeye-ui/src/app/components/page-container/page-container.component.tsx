import { Toolbar } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { PageContainerProps } from "./page-container.interfaces";
import { pageContainerStyles } from "./page-container.styles";

export const PageContainer: FunctionComponent<PageContainerProps> = (
    props: PageContainerProps
) => {
    const pageContainerClasses = pageContainerStyles();

    return (
        <main className={pageContainerClasses.main}>
            {/* Required to clip the container under AppBar */}
            <Toolbar />

            <div className={pageContainerClasses.container}>
                {/* Include children */}
                {props.children}
            </div>
        </main>
    );
};
