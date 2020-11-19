import { Box, Breadcrumbs, Link, Toolbar } from "@material-ui/core";
import NavigateNextIcon from "@material-ui/icons/NavigateNext";
import React, { FunctionComponent } from "react";
import { useHistory } from "react-router-dom";
import { useApplicationBreadcrumbsStore } from "../../store/application-breadcrumbs/application-breadcrumbs.store";
import { Dimension } from "../../utils/material-ui/dimension.util";
import { Palette } from "../../utils/material-ui/palette.util";
import { applicationBreadcrumbsStyles } from "./application-breadcrumbs.styles";

export const ApplicationBreadcrumbs: FunctionComponent = () => {
    const applicationBreadcrumbsClasses = applicationBreadcrumbsStyles();
    const history = useHistory();

    const [
        routerBreadcrumb,
        pageBreadcrumbs,
    ] = useApplicationBreadcrumbsStore((state) => [
        state.routerBreadcrumb,
        state.pageBreadcrumbs,
    ]);

    const onBreadcrumbClick = (path: string): void => {
        history.push(path);
    };

    return (
        <Box
            borderBottom={Dimension.WIDTH_BORDER_DEFAULT}
            color={Palette.COLOR_BORDER_DEFAULT}
        >
            {/* Required to appropriately layout children in Breadcrumbs */}
            <Toolbar variant="dense">
                <Breadcrumbs separator={<NavigateNextIcon />}>
                    {/* Render router breadcrumb */}
                    {routerBreadcrumb?.text && (
                        <Link
                            component="button"
                            variant="subtitle1"
                            onClick={(): void => {
                                onBreadcrumbClick(routerBreadcrumb.path);
                            }}
                        >
                            {routerBreadcrumb.text}
                        </Link>
                    )}

                    {/* Render page breadcrumbs */}
                    {pageBreadcrumbs?.map((breadcrumb, index) => (
                        <Link
                            className={
                                index === pageBreadcrumbs.length - 1
                                    ? applicationBreadcrumbsClasses.selected
                                    : ""
                            }
                            component="button"
                            disabled={index === pageBreadcrumbs.length - 1}
                            key={index}
                            variant="subtitle1"
                            onClick={(): void => {
                                onBreadcrumbClick(breadcrumb.path);
                            }}
                        >
                            {breadcrumb.text}
                        </Link>
                    ))}
                </Breadcrumbs>
            </Toolbar>
        </Box>
    );
};
