import { AppBar, Fab, Link, Menu, MenuItem, Toolbar } from "@material-ui/core";
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
    getHomePath,
    getSignInPath,
    getSignOutPath,
} from "../../utils/routes-util/routes-util";
import { useApplicationBarStyles } from "./application-bar.styles";

export const ApplicationBar: FunctionComponent = () => {
    const applicationBarClasses = useApplicationBarStyles();
    const [auth] = useAuthStore((state) => [state.auth]);
    const [
        optionsAnchorElement,
        setOptionsAnchorElement,
    ] = useState<HTMLElement | null>();
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

    const onCreateAlert = (): void => {
        history.push(getAlertsCreatePath());

        closeAlertOptions();
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

    const onAlertOptionsClick = (event: MouseEvent<HTMLElement>): void => {
        setOptionsAnchorElement(event.currentTarget);
    };

    const closeAlertOptions = (): void => {
        setOptionsAnchorElement(null);
    };

    return (
        <AppBar className={applicationBarClasses.appBar}>
            {/* Required to appropriately layout children in AppBar */}
            <Toolbar>
                {/* ThirdEye logo */}
                <Link
                    className={applicationBarClasses.logo}
                    component="button"
                    onClick={onLogoClick}
                >
                    <ThirdEye width={48} />
                </Link>

                {/* Home */}
                <Link
                    className={classnames(
                        applicationBarClasses.link,
                        isRouteCurrent(ApplicationRoute.HOME)
                            ? applicationBarClasses.selected
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
                        applicationBarClasses.link,
                        isRouteCurrent(ApplicationRoute.ALERTS)
                            ? applicationBarClasses.selected
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
                        applicationBarClasses.link,
                        isRouteCurrent(ApplicationRoute.ANOMALIES)
                            ? applicationBarClasses.selected
                            : ""
                    )}
                    component="button"
                    variant="subtitle2"
                    onClick={onAnomaliesClick}
                >
                    {t("label.anomalies")}
                </Link>

                {(auth && (
                    <>
                        {/* Create alert */}
                        <Fab
                            className={classnames(
                                applicationBarClasses.link,
                                applicationBarClasses.rightAlign
                            )}
                            color="primary"
                            size="small"
                            onClick={onAlertOptionsClick}
                        >
                            <Add />
                        </Fab>

                        <Menu
                            anchorEl={optionsAnchorElement}
                            open={Boolean(optionsAnchorElement)}
                            onClose={closeAlertOptions}
                        >
                            <MenuItem onClick={onCreateAlert}>
                                {t("label.create-alert")}
                            </MenuItem>

                            <MenuItem disabled>
                                {"Create Subscription Group"}
                            </MenuItem>
                        </Menu>

                        {/* Sign out */}
                        <Link
                            className={classnames(
                                applicationBarClasses.link,
                                isRouteCurrent(ApplicationRoute.SIGN_OUT)
                                    ? applicationBarClasses.selected
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
                            applicationBarClasses.link,
                            applicationBarClasses.rightAlign,
                            isRouteCurrent(ApplicationRoute.SIGN_IN)
                                ? applicationBarClasses.selected
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
        </AppBar>
    );
};
