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
import { ReactComponent as ThirdEye } from "../../../assets/icons/third-eye.svg";
import { useAuthStore } from "../../store/auth-store/auth-store";
import {
    ApplicationRoute,
    getAlertsCreatePath,
    getAlertsPath,
    getAnomaliesAllPath,
    getBasePath,
    getConfigurationPath,
    getHomePath,
    getSignInPath,
    getSignOutPath,
} from "../../utils/routes-util/routes-util";
import { useAppBarStyles } from "./app-bar.styles";

export const AppBar: FunctionComponent = () => {
    const appBarClasses = useAppBarStyles();
    const [
        optionsAnchorElement,
        setOptionsAnchorElement,
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

    const onCreateOptionsClick = (event: MouseEvent<HTMLElement>): void => {
        setOptionsAnchorElement(event.currentTarget);
    };

    const onCreateAlert = (): void => {
        history.push(getAlertsCreatePath());

        closeCreateOptions();
    };

    const onCreateSubscriptionGroup = (): void => {
        // TODO

        closeCreateOptions();
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

    const closeCreateOptions = (): void => {
        setOptionsAnchorElement(null);
    };

    return (
        <MuiAppBar className={appBarClasses.appBar}>
            {/* Required to appropriately layout children in AppBar */}
            <Toolbar>
                {/* ThirdEye logo */}
                <Link
                    className={appBarClasses.logo}
                    component="button"
                    onClick={onLogoClick}
                >
                    <ThirdEye width={48} />
                </Link>

                {/* Home */}
                <Link
                    className={classnames(
                        appBarClasses.link,
                        isRouteCurrent(ApplicationRoute.HOME)
                            ? appBarClasses.selected
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
                        isRouteCurrent(ApplicationRoute.ALERTS)
                            ? appBarClasses.selected
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
                        isRouteCurrent(ApplicationRoute.ANOMALIES)
                            ? appBarClasses.selected
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
                        isRouteCurrent(ApplicationRoute.CONFIGURATION)
                            ? appBarClasses.selected
                            : ""
                    )}
                    component="button"
                    variant="subtitle2"
                    onClick={onConfigurationClick}
                >
                    {t("label.configuration")}
                </Link>

                {(auth && (
                    <>
                        {/* Create options */}
                        <Fab
                            className={classnames(
                                appBarClasses.link,
                                appBarClasses.rightAlign
                            )}
                            color="primary"
                            size="small"
                            onClick={onCreateOptionsClick}
                        >
                            <Add />
                        </Fab>

                        <Menu
                            anchorEl={optionsAnchorElement}
                            open={Boolean(optionsAnchorElement)}
                            onClose={closeCreateOptions}
                        >
                            {/* Create alert */}
                            <MenuItem onClick={onCreateAlert}>
                                {t("label.create-alert")}
                            </MenuItem>

                            {/* Create subscription group */}
                            <MenuItem
                                disabled
                                onClick={onCreateSubscriptionGroup}
                            >
                                {"Create Subscription Group"}
                            </MenuItem>
                        </Menu>

                        {/* Sign out */}
                        <Link
                            className={classnames(
                                appBarClasses.link,
                                isRouteCurrent(ApplicationRoute.SIGN_OUT)
                                    ? appBarClasses.selected
                                    : ""
                            )}
                            component="button"
                            variant="subtitle2"
                            onClick={onSignOutClick}
                        >
                            {t("label.sign-out")}
                        </Link>
                    </>
                )) || (
                    // Sign in
                    <Link
                        className={classnames(
                            appBarClasses.link,
                            appBarClasses.rightAlign,
                            isRouteCurrent(ApplicationRoute.SIGN_IN)
                                ? appBarClasses.selected
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
