import { Link } from "@material-ui/core";
import classNames from "classnames";
import React, { FunctionComponent } from "react";
// eslint-disable-next-line no-restricted-imports
import { Link as RouterLink } from "react-router-dom";
import { LinkV1Props } from "./link-v1.interfaces";
import { useLinkV1Styles } from "./link-v1.styles";

export const LinkV1: FunctionComponent<LinkV1Props> = ({
    href,
    externalLink,
    target,
    disabled,
    noWrap,
    color,
    variant,
    className,
    onClick,
    children,
    ...otherProps
}) => {
    const linkV1Classes = useLinkV1Styles();

    return (
        <>
            {href && externalLink && !disabled && (
                // Material UI link
                <Link
                    {...otherProps}
                    className={classNames(className, "link-v1")}
                    color={color}
                    href={href}
                    noWrap={noWrap}
                    target={target}
                    variant={variant}
                >
                    {children}
                </Link>
            )}

            {href && !externalLink && !disabled && (
                // Router link
                <Link
                    {...otherProps}
                    className={classNames(className, "link-v1")}
                    color={color}
                    component={RouterLink}
                    noWrap={noWrap}
                    target={target}
                    to={href}
                    variant={variant}
                >
                    {children}
                </Link>
            )}

            {(!href || disabled) && (
                // Link with click handler
                <Link
                    {...otherProps}
                    className={classNames(
                        { [linkV1Classes.linkDisabled]: disabled },
                        className,
                        "link-v1"
                    )}
                    color={color}
                    component="button"
                    disabled={disabled}
                    noWrap={noWrap}
                    type="button" // Without type, the underlying button is of type submit and interferes with form
                    variant={variant}
                    onClick={onClick}
                >
                    {children}
                </Link>
            )}
        </>
    );
};
