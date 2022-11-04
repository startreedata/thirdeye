import {
    Breadcrumbs as MUIBreadcrumbs,
    Link,
    Typography,
} from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { Link as ReactDomLink } from "react-router-dom";
import { BreadcrumbsProps } from "./breadcrumbs.interfaces";

export const Breadcrumbs: FunctionComponent<BreadcrumbsProps> = ({
    crumbs,
}) => {
    return (
        <MUIBreadcrumbs>
            {crumbs.map((crumb, idx) => {
                if (crumb.link) {
                    return (
                        <Link
                            component={ReactDomLink}
                            key={idx}
                            to={crumb.link}
                        >
                            {crumb.label}
                        </Link>
                    );
                }

                return (
                    <Typography color="textSecondary" key={idx}>
                        {crumb.label}
                    </Typography>
                );
            })}
        </MUIBreadcrumbs>
    );
};
