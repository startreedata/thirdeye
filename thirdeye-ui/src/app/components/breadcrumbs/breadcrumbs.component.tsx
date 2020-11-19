import { AppBar, Breadcrumbs, Toolbar } from "@material-ui/core";
import React, { FunctionComponent, ReactNode } from "react";
import { breadcrumbsStyles } from "./breadcrumbs.styles";

type Props = {
    children: ReactNode;
};

export const CustomBreadcrumbs: FunctionComponent<Props> = ({
    children,
}: Props) => {
    const breadcrumbClasses = breadcrumbsStyles();

    return (
        <AppBar
            className={breadcrumbClasses.appBar}
            color="default"
            position="relative"
        >
            <Toolbar variant="dense">
                <Breadcrumbs>{children}</Breadcrumbs>
            </Toolbar>
        </AppBar>
    );
};
