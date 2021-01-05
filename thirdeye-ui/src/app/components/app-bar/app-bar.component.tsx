import {
    AppBar as MuiAppBar,
    Fab,
    Link,
    Menu,
    MenuItem,
    Toolbar,
} from "@material-ui/core";
import { Add } from "@material-ui/icons";
import classnames from "classnames";
import React, { FunctionComponent, MouseEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useLocation } from "react-router-dom";
import { ReactComponent as ThirdEye } from "../../../assets/images/third-eye.svg";
import { useAuthStore } from "../../store/auth-store/auth-store";
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
} from "../../utils/routes-util/routes-util";
import { useAppBarStyles } from "./app-bar.styles";

export const AppBar: FunctionComponent = () => {
    const appBarClasses = useAppBarStyles();
    const [
        shortcutOptionsAnchorElement,
        setShortcutOptionsAnchorElement,
    ] = useState<HTMLElement | null>();
    const [auth] = useAuthStore((state) => [state.auth]);
    const history = useHistory();
    const location = useLocation();
    const { t } = useTranslation();

    const onLogoClick = (): void => {
        history.push(getBasePath());
    };

    const onHomeClick = (): void => {
        history.push(getHomePath());
    };

    const onAlertsClick = (): void => {
        history.push(getAlertsPath());
    };

    const onAnomaliesClick = (): void => {
        history.push(getAnomaliesAllPath());
    };

    const onConfigurationClick = (): void => {
        history.push(getConfigurationPath());
    };

    const onShortcutOptionsClick = (event: MouseEvent<HTMLElement>): void => {
        setShortcutOptionsAnchorElement(event.currentTarget);
    };

    const onCloseShortcutOptions = (): void => {
        setShortcutOptionsAnchorElement(null);
    };

    const onCreateAlert = (): void => {
        history.push(getAlertsCreatePath());

        onCloseShortcutOptions();
    };

    const onCreateSubscriptionGroup = (): void => {
        history.push(getSubscriptionGroupsCreatePath());

        onCloseShortcutOptions();
    };

    const onSignInClick = (): void => {
        history.push(getSignInPath());
    };

    const onSignOutClick = (): void => {
        history.push(getSignOutPath());
    };

    const isRouteCurrent = (route: string): boolean => {
        return location.pathname.indexOf(route) === 0;
    };

    return (
        <MuiAppBar className={appBarClasses.container}>
            <Toolbar>
                {/* ThirdEye logo */}
                <Link
                    className={appBarClasses.link}
                    component="button"
                    onClick={onLogoClick}
                >
                    <ThirdEye width={48} />
                </Link>

                {/* Home */}
                <Link
                    className={classnames(
                        appBarClasses.link,
                        isRouteCurrent(AppRoute.HOME)
                            ? appBarClasses.selectedLink
                            : ""
                    )}
                    component="button"
                    variant="subtitle2"
                    onClick={onHomeClick}
                >
                    {t("label.home")}
                </Link>

                {/* Alerts */}
                <Link
                    className={classnames(
                        appBarClasses.link,
                        isRouteCurrent(AppRoute.ALERTS)
                            ? appBarClasses.selectedLink
                            : ""
                    )}
                    component="button"
                    variant="subtitle2"
                    onClick={onAlertsClick}
                >
                    {t("label.alerts")}
                </Link>

                {/* Anomalies */}
                <Link
                    className={classnames(
                        appBarClasses.link,
                        isRouteCurrent(AppRoute.ANOMALIES)
                            ? appBarClasses.selectedLink
                            : ""
                    )}
                    component="button"
                    variant="subtitle2"
                    onClick={onAnomaliesClick}
                >
                    {t("label.anomalies")}
                </Link>

                {/* Configuration */}
                <Link
                    className={classnames(
                        appBarClasses.link,
                        isRouteCurrent(AppRoute.CONFIGURATION)
                            ? appBarClasses.selectedLink
                            : ""
                    )}
                    component="button"
                    variant="subtitle2"
                    onClick={onConfigurationClick}
                >
                    {t("label.configuration")}
                </Link>

                {auth && (
                    <>
                        {/* Shortcut options */}
                        <Fab
                            className={classnames(appBarClasses.rightAlign)}
                            color="primary"
                            size="small"
                            onClick={onShortcutOptionsClick}
                        >
                            <Add />
                        </Fab>

                        <Menu
                            anchorEl={shortcutOptionsAnchorElement}
                            open={Boolean(shortcutOptionsAnchorElement)}
                            onClose={onCloseShortcutOptions}
                        >
                            {/* Create alert */}
                            <MenuItem onClick={onCreateAlert}>
                                {t("label.create-alert")}
                            </MenuItem>

                            {/* Create subscription group */}
                            <MenuItem onClick={onCreateSubscriptionGroup}>
                                {"Create Subscription Group"}
                            </MenuItem>
                        </Menu>

                        {/* Sign out */}
                        <Link
                            className={classnames(
                                appBarClasses.link,
                                isRouteCurrent(AppRoute.SIGN_OUT)
                                    ? appBarClasses.selectedLink
                                    : ""
                            )}
                            component="button"
                            variant="subtitle2"
                            onClick={onSignOutClick}
                        >
                            {t("label.sign-out")}
                        </Link>
                    </>
                )}

                {!auth && (
                    // Sign in
                    <Link
                        className={classnames(
                            appBarClasses.link,
                            appBarClasses.rightAlign,
                            isRouteCurrent(AppRoute.SIGN_IN)
                                ? appBarClasses.selectedLink
                                : ""
                        )}
                        component="button"
                        variant="subtitle2"
                        onClick={onSignInClick}
                    >
                        {t("label.sign-in")}
                    </Link>
                )}
            </Toolbar>
        </MuiAppBar>
    );
};
