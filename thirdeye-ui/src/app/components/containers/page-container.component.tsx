import { Toolbar } from "@material-ui/core";
import classnames from "classnames";
import React, { FunctionComponent, ReactNode } from "react";
import { pageContainerStyles } from "./page-container.styles";

type Props = {
    children?: ReactNode;
    centered?: boolean;
};

export const PageContainer: FunctionComponent<Props> = ({
    children,
    centered,
}: Props) => {
    const pageContainerClasses = pageContainerStyles();

    return (
        <main
            className={classnames(
                pageContainerClasses.main,
                centered ? pageContainerClasses.centered : ""
            )}
        >
            {/* Required to clip the container under AppBar */}
            <Toolbar />

            <div>
                {/* Include children */}
                {children}
            </div>
        </main>
    );
};

PageContainer.defaultProps = {
    centered: true,
};
