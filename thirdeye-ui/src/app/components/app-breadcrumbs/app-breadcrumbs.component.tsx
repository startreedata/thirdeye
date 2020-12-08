import { Box, Breadcrumbs, Link, Toolbar } from "@material-ui/core";
import { NavigateNext } from "@material-ui/icons";
import React, { FunctionComponent } from "react";
import { useHistory } from "react-router-dom";
import { Dimension } from "../../utils/material-ui-util/dimension-util";
import { Palette } from "../../utils/material-ui-util/palette-util";
import { AppBreadcrumbsProps } from "./app-breadcrumbs.interfaces";
import { useAppBreadcrumbsStyles } from "./app-breadcrumbs.styles";

export const AppBreadcrumbs: FunctionComponent<AppBreadcrumbsProps> = (
    props: AppBreadcrumbsProps
) => {
    const appBreadcrumbsClasses = useAppBreadcrumbsStyles();
    const history = useHistory();

    const onBreadcrumbClick = (path: string): void => {
        history.push(path);
    };

    return (
        <Box
            borderBottom={Dimension.WIDTH_BORDER_DEFAULT}
            className={appBreadcrumbsClasses.container}
            color={Palette.COLOR_BORDER_DEFAULT}
        >
            {/* Required to appropriately layout children in Breadcrumbs */}
            <Toolbar variant="dense">
                <Breadcrumbs separator={<NavigateNext />}>
                    {props.breadcrumbs.map((breadcrumb, index) => (
                        <Link
                            className={
                                index === props.breadcrumbs.length - 1
                                    ? appBreadcrumbsClasses.selected
                                    : ""
                            }
                            component="button"
                            disabled={index === props.breadcrumbs.length - 1}
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
