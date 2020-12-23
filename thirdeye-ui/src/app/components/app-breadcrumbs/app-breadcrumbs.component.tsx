import { Box, Breadcrumbs, Link, Toolbar } from "@material-ui/core";
import { NavigateNext } from "@material-ui/icons";
import React, { FunctionComponent } from "react";
import { useHistory } from "react-router-dom";
import { Dimension } from "../../utils/material-ui-util/dimension-util";
import { Palette } from "../../utils/material-ui-util/palette-util";
import { AppBreadcrumbsProps, Breadcrumb } from "./app-breadcrumbs.interfaces";
import { useAppBreadcrumbsStyles } from "./app-breadcrumbs.styles";

export const AppBreadcrumbs: FunctionComponent<AppBreadcrumbsProps> = (
    props: AppBreadcrumbsProps
) => {
    const appBreadcrumbsClasses = useAppBreadcrumbsStyles();
    const history = useHistory();

    const onBreadcrumbClick = (breadcrumb: Breadcrumb): void => {
        breadcrumb && breadcrumb.pathFn && history.push(breadcrumb.pathFn());
    };

    return (
        <Box
            border={Dimension.WIDTH_BORDER_DEFAULT}
            borderColor={Palette.COLOR_BORDER_DEFAULT}
            borderLeft={0}
            borderRight={0}
            borderTop={0}
            className={appBreadcrumbsClasses.container}
        >
            {/* Required to appropriately layout children in breadcrumbs */}
            <Toolbar
                classes={{ dense: appBreadcrumbsClasses.dense }}
                variant="dense"
            >
                <Breadcrumbs separator={<NavigateNext />}>
                    {props.breadcrumbs.map((breadcrumb, index) => (
                        <Link
                            className={
                                index === props.breadcrumbs.length - 1
                                    ? appBreadcrumbsClasses.selectedLink
                                    : ""
                            }
                            component="button"
                            disabled={index === props.breadcrumbs.length - 1} // Last breadcrumb disabled
                            display="block"
                            key={index}
                            variant="body1"
                            onClick={(): void => {
                                onBreadcrumbClick(breadcrumb);
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
