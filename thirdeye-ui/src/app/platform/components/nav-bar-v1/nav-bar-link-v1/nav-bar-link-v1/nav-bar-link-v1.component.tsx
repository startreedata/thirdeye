import { Link, ListItem } from "@material-ui/core";
import classNames from "classnames";
import React, {
    createContext,
    FunctionComponent,
    ReactNode,
    useContext,
    useState,
} from "react";
// eslint-disable-next-line no-restricted-imports
import { Link as RouterLink } from "react-router-dom";
import { TooltipV1 } from "../../../tooltip-v1/tooltip-v1.component";
import { useNavBarV1 } from "../../nav-bar-v1/nav-bar-v1.component";
import {
    NavBarLinkV1ContextProps,
    NavBarLinkV1Props,
} from "./nav-bar-link-v1.interfaces";
import { useNavBarLinkV1Styles } from "./nav-bar-link-v1.styles";

export const NavBarLinkV1: FunctionComponent<NavBarLinkV1Props> = ({
    href,
    externalLink,
    target,
    selected,
    className,
    onClick,
    children,
    ...otherProps
}) => {
    const navBarLinkV1Classes = useNavBarLinkV1Styles();
    const [hover, setHover] = useState(false);
    const [tooltip, setTooltip] = useState<ReactNode>("");
    const { navBarMinimized } = useNavBarV1();

    const handleMouseEnter = (): void => {
        setHover(true);
    };

    const handleMouseLeave = (): void => {
        setHover(false);
    };

    const navBarLinkV1Context = {
        hover: hover,
        selected: Boolean(selected),
        setTooltip: setTooltip,
    };

    return (
        <NavBarLinkV1Context.Provider value={navBarLinkV1Context}>
            <TooltipV1
                className="nav-bar-link-v1-tooltip"
                delay={0}
                placement="right"
                title={tooltip}
                visible={navBarMinimized && Boolean(tooltip)} // Tooltip only when nav bar minimized
            >
                <div>
                    {href && externalLink && (
                        // List item as a link
                        <ListItem
                            {...otherProps}
                            button
                            className={classNames(
                                navBarLinkV1Classes.navBarLink,
                                className,
                                "nav-bar-link-v1"
                            )}
                            classes={{
                                gutters: navBarLinkV1Classes.navBarLinkGutters,
                            }}
                            component={Link}
                            href={href}
                            target={target}
                            onMouseEnter={handleMouseEnter}
                            onMouseLeave={handleMouseLeave}
                        >
                            {children}
                        </ListItem>
                    )}

                    {href && !externalLink && (
                        // List item as a router link
                        <ListItem
                            {...otherProps}
                            button
                            className={classNames(
                                navBarLinkV1Classes.navBarLink,
                                className,
                                "nav-bar-link-v1"
                            )}
                            classes={{
                                gutters: navBarLinkV1Classes.navBarLinkGutters,
                            }}
                            component={RouterLink}
                            target={target}
                            to={href}
                            onMouseEnter={handleMouseEnter}
                            onMouseLeave={handleMouseLeave}
                        >
                            {children}
                        </ListItem>
                    )}

                    {!href && (
                        // List item with click handler
                        <ListItem
                            {...otherProps}
                            button
                            className={classNames(
                                navBarLinkV1Classes.navBarLink,
                                className,
                                "nav-bar-link-v1"
                            )}
                            classes={{
                                gutters: navBarLinkV1Classes.navBarLinkGutters,
                            }}
                            onClick={onClick}
                            onMouseEnter={handleMouseEnter}
                            onMouseLeave={handleMouseLeave}
                        >
                            {children}
                        </ListItem>
                    )}
                </div>
            </TooltipV1>
        </NavBarLinkV1Context.Provider>
    );
};

const NavBarLinkV1Context = createContext<NavBarLinkV1ContextProps>(
    {} as NavBarLinkV1ContextProps
);

export const useNavBarLinkV1 = (): NavBarLinkV1ContextProps => {
    return useContext(NavBarLinkV1Context);
};
