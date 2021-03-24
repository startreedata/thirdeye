import {
    AppBar as MuiAppBar,
    Fab,
    Hidden,
    IconButton,
    Link,
    Menu,
    MenuItem,
    Toolbar,
} from "@material-ui/core";
import AddIcon from "@material-ui/icons/Add";
import MenuIcon from "@material-ui/icons/Menu";
import PersonIcon from "@material-ui/icons/Person";
import classnames from "classnames";
import React, { FunctionComponent, MouseEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useLocation } from "react-router-dom";
import { ReactComponent as ThirdEyeIcon } from "../../../assets/images/third-eye.svg";
import {
    AppRoute,
    getAlertsCreatePath,
    getAlertsPath,
    getAnomaliesAllPath,
    getBasePath,
    getConfigurationPath,
    getHomePath,
    getSignInPath,
    getSignOutPath,
    getSubscriptionGroupsCreatePath,
} from "../../utils/routes/routes.util";
import { AppBarDrawer } from "../app-bar-drawer/app-bar-drawer.component";
import { useAuth } from "../auth-provider/auth-provider.component";
import { useAppBarStyles } from "./app-bar.styles";

const ELEVATION_APP_BAR = 6;
const HEIGHT_LOGO = 32;

export const AppBar: FunctionComponent = () => {
    const appBarClasses = useAppBarStyles();
    const [appBarDrawerOpen, setAppBarDrawerOpen] = useState(false);
    const [
        shortcutOptionsAnchorElement,
        setShortcutOptionsAnchorElement,
    ] = useState<HTMLElement | null>();
    const [
        accountOptionsAnchorElement,
        setAccountOptionsAnchorElement,
    ] = useState<HTMLElement | null>();
    const { authDisabled, authenticated } = useAuth();
    const history = useHistory();
    const location = useLocation();
    const { t } = useTranslation();

    const handleAppBarDrawerClick = (): void => {
        setAppBarDrawerOpen(true);
    };

    const handleAppBarDrawerClose = (): void => {
        setAppBarDrawerOpen(false);
    };

    const handleLogoClick = (): void => {
        history.push(getBasePath());
    };

    const handleHomeClick = (): void => {
        history.push(getHomePath());
    };

    const handleAlertsClick = (): void => {
        history.push(getAlertsPath());
    };

    const handleAnomaliesClick = (): void => {
        history.push(getAnomaliesAllPath());
    };

    const handleConfigurationClick = (): void => {
        history.push(getConfigurationPath());
    };

    const handleSignIn = (): void => {
        history.push(getSignInPath());
    };

    const handleShortcutOptionsClick = (
        event: MouseEvent<HTMLElement>
    ): void => {
        setShortcutOptionsAnchorElement(event.currentTarget);
    };

    const handleShortcutOptionsClose = (): void => {
        setShortcutOptionsAnchorElement(null);
    };

    const handleCreateAlert = (): void => {
        history.push(getAlertsCreatePath());
        handleShortcutOptionsClose();
    };

    const handleCreateSubscriptionGroup = (): void => {
        history.push(getSubscriptionGroupsCreatePath());
        handleShortcutOptionsClose();
    };

    const handleAccountOptionsClick = (
        event: MouseEvent<HTMLElement>
    ): void => {
        setAccountOptionsAnchorElement(event.currentTarget);
    };

    const handleAccountOptionsClose = (): void => {
        setAccountOptionsAnchorElement(null);
    };

    const handleSignOut = (): void => {
        history.push(getSignOutPath());
        handleAccountOptionsClose();
    };

    const isRouteCurrent = (route: string): boolean => {
        return location.pathname.indexOf(route) === 0;
    };

    return (
        <MuiAppBar
            className={appBarClasses.appBar}
            elevation={ELEVATION_APP_BAR}
        >
            <Toolbar>
                <Hidden smUp>
                    {/* App bar drawer button */}
                    <IconButton edge="start" onClick={handleAppBarDrawerClick}>
                        <MenuIcon />
                    </IconButton>

                    {/* App bar drawer */}
                    <AppBarDrawer
                        open={appBarDrawerOpen}
                        onClose={handleAppBarDrawerClose}
                    />
                </Hidden>

                {/* ThirdEye logo */}
                <Link
                    className={appBarClasses.logo}
                    component="button"
                    onClick={handleLogoClick}
                >
                    <ThirdEyeIcon height={HEIGHT_LOGO} />
                </Link>

                <Hidden xsDown>
                    {/* Home */}
                    <Link
                        className={appBarClasses.link}
                        color={
                            isRouteCurrent(AppRoute.HOME)
                                ? "textPrimary"
                                : "primary"
                        }
                        component="button"
                        variant="subtitle1"
                        onClick={handleHomeClick}
                    >
                        {t("label.home")}
                    </Link>

                    {/* Alerts */}
                    <Link
                        className={appBarClasses.link}
                        color={
                            isRouteCurrent(AppRoute.ALERTS)
                                ? "textPrimary"
                                : "primary"
                        }
                        component="button"
                        variant="subtitle1"
                        onClick={handleAlertsClick}
                    >
                        {t("label.alerts")}
                    </Link>

                    {/* Anomalies */}
                    <Link
                        className={appBarClasses.link}
                        color={
                            isRouteCurrent(AppRoute.ANOMALIES)
                                ? "textPrimary"
                                : "primary"
                        }
                        component="button"
                        variant="subtitle1"
                        onClick={handleAnomaliesClick}
                    >
                        {t("label.anomalies")}
                    </Link>

                    {/* Configuration */}
                    <Link
                        className={appBarClasses.link}
                        color={
                            isRouteCurrent(AppRoute.CONFIGURATION)
                                ? "textPrimary"
                                : "primary"
                        }
                        component="button"
                        variant="subtitle1"
                        onClick={handleConfigurationClick}
                    >
                        {t("label.configuration")}
                    </Link>
                </Hidden>

                {!authDisabled && !authenticated && (
                    // Sign in
                    <Link
                        className={classnames(
                            appBarClasses.link,
                            appBarClasses.linkRightAligned
                        )}
                        color={
                            isRouteCurrent(AppRoute.SIGN_IN)
                                ? "textPrimary"
                                : "primary"
                        }
                        component="button"
                        variant="subtitle1"
                        onClick={handleSignIn}
                    >
                        {t("label.sign-in")}
                    </Link>
                )}

                {(authDisabled || authenticated) && (
                    <>
                        {/* Shortcut options button */}
                        <Fab
                            className={classnames(
                                appBarClasses.link,
                                appBarClasses.linkRightAligned
                            )}
                            color="primary"
                            size="small"
                            onClick={handleShortcutOptionsClick}
                        >
                            <AddIcon />
                        </Fab>

                        {/* Shortcut options */}
                        <Menu
                            anchorEl={shortcutOptionsAnchorElement}
                            open={Boolean(shortcutOptionsAnchorElement)}
                            onClose={handleShortcutOptionsClose}
                        >
                            {/* Create alert */}
                            <MenuItem onClick={handleCreateAlert}>
                                {t("label.create-entity", {
                                    entity: t("label.alert"),
                                })}
                            </MenuItem>

                            {/* Create subscription group */}
                            <MenuItem onClick={handleCreateSubscriptionGroup}>
                                {t("label.create-entity", {
                                    entity: t("label.subscription-group"),
                                })}
                            </MenuItem>
                        </Menu>
                    </>
                )}

                {!authDisabled && authenticated && (
                    <>
                        {/* Account options button */}
                        <IconButton
                            edge="end"
                            onClick={handleAccountOptionsClick}
                        >
                            <PersonIcon />
                        </IconButton>

                        {/* Account options */}
                        <Menu
                            anchorEl={accountOptionsAnchorElement}
                            open={Boolean(accountOptionsAnchorElement)}
                            onClose={handleAccountOptionsClose}
                        >
                            {/* Sign out */}
                            <MenuItem onClick={handleSignOut}>
                                {t("label.sign-out")}
                            </MenuItem>
                        </Menu>
                    </>
                )}
            </Toolbar>
        </MuiAppBar>
    );
};
