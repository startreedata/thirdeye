import { Breadcrumbs as MuiBreadcrumbs, Link } from "@material-ui/core";
import { NavigateNext } from "@material-ui/icons";
import React, { FunctionComponent } from "react";
import { BreadcrumbsProps } from "./breadcrumbs.interfaces";

const MAX_ITEMS = 5;

export const Breadcrumbs: FunctionComponent<BreadcrumbsProps> = (
    props: BreadcrumbsProps
) => {
    return (
        <MuiBreadcrumbs
            itemsAfterCollapse={props.trailingSeparator ? 2 : 1}
            maxItems={
                props.maxItems
                    ? props.trailingSeparator
                        ? props.maxItems + 1
                        : props.maxItems
                    : MAX_ITEMS
            }
            separator={<NavigateNext fontSize="small" />}
        >
            {/* Breadcrumbs */}
            {props.breadcrumbs &&
                props.breadcrumbs.map((breadcrumb, index) => (
                    <Link
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
