import { Toolbar } from "@material-ui/core";
import classnames from "classnames";
import React, { FunctionComponent, ReactNode } from "react";
import { pageContainerStyles } from "./page-container.styles";

type Props = {
    children?: ReactNode;
    centered?: boolean;
    noPadding?: boolean;
    breadcrumbs?: ReactNode;
};

export const PageContainer: FunctionComponent<Props> = ({
    children,
    centered,
    noPadding,
    breadcrumbs,
}: Props) => {
    const pageContainerClasses = pageContainerStyles();

    return (
        <main
            className={classnames(
                pageContainerClasses.main,
                noPadding ? pageContainerClasses.padding0 : ""
            )}
        >
            {/* Required to clip the container under AppBar */}
            <Toolbar />

            {/* Add breadcrumbs at generic level */}
            {breadcrumbs}

            <div
                className={classnames(
                    pageContainerClasses.innerContainer,
                    centered ? pageContainerClasses.centered : ""
                )}
            >
                {/* Include children */}
                {children}
            </div>
        </main>
    );
};

PageContainer.defaultProps = {
    centered: true,
};
