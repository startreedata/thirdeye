import { ListItemText } from "@material-ui/core";
import classNames from "classnames";
import React, { FunctionComponent, useEffect } from "react";
import { useNavBarV1 } from "../../nav-bar-v1/nav-bar-v1.component";
import { useNavBarLinkV1 } from "../nav-bar-link-v1/nav-bar-link-v1.component";
import { NavBarLinkTextV1Props } from "./nav-bar-link-text-v1.interfaces";
import { useNavBarLinkTextV1Styles } from "./nav-bar-link-text-v1.styles";

export const NavBarLinkTextV1: FunctionComponent<NavBarLinkTextV1Props> = ({
    className,
    children,
    ...otherProps
}) => {
    const navBarLinkTextV1Classes = useNavBarLinkTextV1Styles();
    const { navBarMinimized } = useNavBarV1();
    const { hover, selected, setTooltip } = useNavBarLinkV1();

    useEffect(() => {
        // Set children as tooltip
        setTooltip(children);
    }, [children]);

    return (
        <>
            {!navBarMinimized && (
                // Visible only when nav bar is maximized
                <ListItemText
                    {...otherProps}
                    className={classNames(
                        hover || selected
                            ? navBarLinkTextV1Classes.navBarLinkTextHover
                            : navBarLinkTextV1Classes.navBarLinkTextRegular,
                        className,
                        "nav-bar-link-text-v1"
                    )}
                    primary={children}
                    primaryTypographyProps={{
                        variant: hover || selected ? "subtitle2" : "body2",
                    }}
                />
            )}
        </>
    );
};
