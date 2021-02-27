import { Breadcrumbs as MuiBreadcrumbs, Link } from "@material-ui/core";
import { classnames } from "@material-ui/data-grid";
import NavigateNextIcon from "@material-ui/icons/NavigateNext";
import React, { FunctionComponent } from "react";
import { BreadcrumbsProps } from "./breadcrumbs.interfaces";
import { useBreadcrumbsStyles } from "./breadcrumbs.styles";

const MAX_ITEMS_BREADCRUMBS = 5;

export const Breadcrumbs: FunctionComponent<BreadcrumbsProps> = (
    props: BreadcrumbsProps
) => {
    const breadcrumbsClasses = useBreadcrumbsStyles(props);

    return (
        <MuiBreadcrumbs
            itemsAfterCollapse={props.trailingSeparator ? 2 : 1}
            maxItems={
                props.maxItems
                    ? props.trailingSeparator
                        ? props.maxItems + 1
                        : props.maxItems
                    : MAX_ITEMS_BREADCRUMBS
            }
            separator={<NavigateNextIcon fontSize="small" />}
        >
            {/* Breadcrumbs */}
            {props.breadcrumbs &&
                props.breadcrumbs.map((breadcrumb, index) => (
                    <Link
                        noWrap
                        className={classnames({
                            [breadcrumbsClasses.maxWidthBreadcrumb]:
                                props.breadcrumbMaxWidth,
                        })}
                        color={breadcrumb.onClick ? "primary" : "textSecondary"}
                        component="button"
                        disabled={!breadcrumb.onClick}
                        display="block"
                        key={index}
                        variant={props.variant || "subtitle2"}
                        onClick={breadcrumb.onClick}
                    >
                        {breadcrumb.text}
                    </Link>
                ))}

            {/* Trailing separator */}
            {props.trailingSeparator && <span />}
        </MuiBreadcrumbs>
    );
};
