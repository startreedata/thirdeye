import { Box, Toolbar } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { useAppBreadcrumbsStore } from "../../store/app-breadcrumbs-store/app-breadcrumbs-store";
import { Dimension } from "../../utils/material-ui-util/dimension-util";
import { Palette } from "../../utils/material-ui-util/palette-util";
import { Breadcrumbs } from "../breadcrumbs/breadcrumbs.component";
import { UseAppBreadcrumbsProps } from "./app-breadcrumbs.interfaces";
import { useAppBreadcrumbsStyles } from "./app-breadcrumbs.styles";

export const AppBreadcrumbs: FunctionComponent = () => {
    const appBreadcrumbsClasses = useAppBreadcrumbsStyles();
    const [
        routerBreadcrumbs,
        pageBreadcrumbs,
    ] = useAppBreadcrumbsStore((state) => [
        state.routerBreadcrumbs,
        state.pageBreadcrumbs,
    ]);

    return (
        <Box
            border={Dimension.WIDTH_BORDER_DEFAULT}
            borderColor={Palette.COLOR_BORDER_DEFAULT}
            borderLeft={0}
            borderRight={0}
            borderTop={0}
            className={appBreadcrumbsClasses.container}
        >
            <Toolbar
                classes={{ dense: appBreadcrumbsClasses.dense }}
                variant="dense"
            >
                {/* Breadcrumbs */}
                <Breadcrumbs
                    breadcrumbs={[...routerBreadcrumbs, ...pageBreadcrumbs]}
                />
            </Toolbar>
        </Box>
    );
};

export const useAppBreadcrumbs = (): UseAppBreadcrumbsProps => {
    return useAppBreadcrumbsStore((state) => ({
        setRouterBreadcrumbs: state.setRouterBreadcrumbs,
        setPageBreadcrumbs: state.setPageBreadcrumbs,
        pushPageBreadcrumb: state.pushPageBreadcrumb,
        popPageBreadcrumb: state.popPageBreadcrumb,
    }));
};
