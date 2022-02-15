// Copyright 2021 StarTree Inc.
// All rights reserved. Confidential and proprietary information of StarTree Inc.
import { Drawer, useMediaQuery, useTheme } from "@material-ui/core";
import ArrowBackIosOutlinedIcon from "@material-ui/icons/ArrowBackIosOutlined";
import ArrowForwardIosOutlinedIcon from "@material-ui/icons/ArrowForwardIosOutlined";
import classNames from "classnames";
import React, {
    createContext,
    FunctionComponent,
    useContext,
    useEffect,
} from "react";
import { ReactComponent as ThirdEye } from "../../../../../../assets/images/thirdeye.svg";
import { useNavBarV1 as useNavBarStoreV1 } from "../../../stores/nav-bar-v1/nav-bar-v1.store";
import { LinkV1 } from "../../link-v1/link-v1.component";
import { NavBarLinkIconV1 } from "../nav-bar-link-v1/nav-bar-link-icon-v1/nav-bar-link-icon-v1.component";
import { NavBarLinkTextV1 } from "../nav-bar-link-v1/nav-bar-link-text-v1/nav-bar-link-text-v1.component";
import { NavBarLinkV1 } from "../nav-bar-link-v1/nav-bar-link-v1/nav-bar-link-v1.component";
import {
    NavBarUserPreferenceV1,
    NavBarV1ContextProps,
    NavBarV1Props,
} from "./nav-bar-v1.interfaces";
import { useNavBarV1Styles } from "./nav-bar-v1.styles";

export const NavBarV1: FunctionComponent<NavBarV1Props> = ({
    homePath,
    minimizeLabel,
    maximizeLabel,
    className,
    children,
    ...otherProps
}: NavBarV1Props) => {
    const navBarV1Classes = useNavBarV1Styles();
    const [
        navBarMinimized,
        navBarUserPreference,
        minimizeNavBar,
        maximizeNavBar,
        setNavBarUserPreference,
    ] = useNavBarStoreV1((state) => [
        state.navBarMinimized,
        state.navBarUserPreference,
        state.minimizeNavBar,
        state.maximizeNavBar,
        state.setNavBarUserPreference,
    ]);
    const theme = useTheme();
    const smDown = useMediaQuery(theme.breakpoints.down("sm"));

    useEffect(() => {
        if (smDown) {
            // Screen width too small for nav bar, minimize
            minimizeNavBar();

            return;
        }

        if (
            !smDown &&
            navBarUserPreference === NavBarUserPreferenceV1.Minimized
        ) {
            // User preference to be honored
            minimizeNavBar();

            return;
        }

        if (
            !smDown &&
            navBarUserPreference === NavBarUserPreferenceV1.Maximized
        ) {
            // User preference to be honored
            maximizeNavBar();

            return;
        }
    }, [navBarUserPreference, smDown]);

    const handleNavBarMinimize = (): void => {
        setNavBarUserPreference(NavBarUserPreferenceV1.Minimized);
    };

    const handleNavBarMaximize = (): void => {
        setNavBarUserPreference(NavBarUserPreferenceV1.Maximized);
    };

    const navBarV1Context = {
        navBarMinimized: navBarMinimized,
    };

    return (
        <NavBarV1Context.Provider value={navBarV1Context}>
            <Drawer
                {...otherProps}
                className={classNames(
                    navBarV1Classes.navBar,
                    navBarMinimized
                        ? navBarV1Classes.navBarMinimized
                        : navBarV1Classes.navBarMaximized,
                    className,
                    "nav-bar-v1"
                )}
                classes={{
                    paper: classNames(
                        navBarV1Classes.navBar,
                        navBarMinimized
                            ? navBarV1Classes.navBarMinimized
                            : navBarV1Classes.navBarMaximized
                    ),
                }}
                variant="permanent"
            >
                {/* StarTree logo */}
                <div
                    className={classNames(
                        navBarV1Classes.navBarHeaderContainer,
                        navBarMinimized
                            ? navBarV1Classes.navBarMinimized
                            : navBarV1Classes.navBarMaximized
                    )}
                >
                    <LinkV1
                        className="nav-bar-v1-header-link"
                        href={homePath || "/"}
                        variant="body2"
                    >
                        <div
                            className={classNames(
                                navBarV1Classes.navBarHeader,
                                navBarMinimized
                                    ? navBarV1Classes.navBarHeaderMinimized
                                    : navBarV1Classes.navBarHeaderMaximized,
                                "nav-bar-v1-header-link-icon"
                            )}
                        >
                            {navBarMinimized && (
                                <ThirdEye
                                    className={navBarV1Classes.navBarHeaderLogo}
                                />
                            )}

                            {!navBarMinimized && (
                                <ThirdEye
                                    className={navBarV1Classes.navBarHeaderLogo}
                                />
                            )}
                        </div>
                    </LinkV1>
                </div>

                <div className={navBarV1Classes.navBarLinks}>
                    {/* Links */}
                    {children}

                    {/* Minimize */}
                    {!smDown &&
                        navBarUserPreference ===
                            NavBarUserPreferenceV1.Maximized && (
                            <NavBarLinkV1
                                className="nav-bar-v1-minimize-link"
                                onClick={handleNavBarMinimize}
                            >
                                <NavBarLinkIconV1 className="nav-bar-v1-minimize-link-icon">
                                    <ArrowBackIosOutlinedIcon />
                                </NavBarLinkIconV1>

                                <NavBarLinkTextV1 className="nav-bar-v1-minimize-link-text">
                                    {minimizeLabel}
                                </NavBarLinkTextV1>
                            </NavBarLinkV1>
                        )}

                    {/* Maximize */}
                    {!smDown &&
                        navBarUserPreference ===
                            NavBarUserPreferenceV1.Minimized && (
                            <NavBarLinkV1
                                className="nav-bar-v1-maximize-link"
                                onClick={handleNavBarMaximize}
                            >
                                <NavBarLinkIconV1 className="nav-bar-v1-maximize-link-icon">
                                    <ArrowForwardIosOutlinedIcon />
                                </NavBarLinkIconV1>

                                <NavBarLinkTextV1 className="nav-bar-v1-maximize-link-text">
                                    {maximizeLabel}
                                </NavBarLinkTextV1>
                            </NavBarLinkV1>
                        )}
                </div>
            </Drawer>
        </NavBarV1Context.Provider>
    );
};

const NavBarV1Context = createContext<NavBarV1ContextProps>(
    {} as NavBarV1ContextProps
);

export const useNavBarV1 = (): NavBarV1ContextProps => {
    return useContext(NavBarV1Context);
};
