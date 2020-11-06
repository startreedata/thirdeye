import { Typography } from "@material-ui/core";
import React, { FunctionComponent } from "react";
import { Link, LinkProps } from "react-router-dom";

export const RouterLink: FunctionComponent<LinkProps> = ({
    to,
    children,
}: LinkProps) => {
    return (
        <Link style={{ textDecoration: "none" }} to={to}>
            <Typography color="primary" variant="subtitle2">
                <strong>{children}</strong>
            </Typography>
        </Link>
    );
};
