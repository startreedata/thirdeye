import { Breadcrumbs as MuiBreadcrumbs, Link } from "@material-ui/core";
import { NavigateNext } from "@material-ui/icons";
import React, { FunctionComponent } from "react";
import { BreadcrumbsProps } from "./breadcrumbs.interfaces";
import { useBreadcrumbsStyles } from "./breadcrumbs.styles";

export const Breadcrumbs: FunctionComponent<BreadcrumbsProps> = (
    props: BreadcrumbsProps
) => {
    const breadcrumbsClasses = useBreadcrumbsStyles();

    return (
        <MuiBreadcrumbs separator={<NavigateNext />}>
            {props.breadcrumbs &&
                props.breadcrumbs
                    .filter((breadcrumb) => Boolean(breadcrumb.text))
                    .map((breadcrumb, index) => (
                        <Link
                            className={
                                // Last breadcrumb/breadcrumb without click handler to be selected
                                index === props.breadcrumbs.length - 1 ||
                                !breadcrumb.onClick
                                    ? breadcrumbsClasses.selectedLink
                                    : ""
                            }
                            component="button"
                            disabled={
                                // Last breadcrumb/breadcrumb without click handler to be disabled
                                index === props.breadcrumbs.length - 1 ||
                                !breadcrumb.onClick
                            }
                            display="block"
                            key={index}
                            variant="subtitle1"
                            onClick={breadcrumb.onClick}
                        >
                            {breadcrumb.text}
                        </Link>
                    ))}
        </MuiBreadcrumbs>
    );
};
