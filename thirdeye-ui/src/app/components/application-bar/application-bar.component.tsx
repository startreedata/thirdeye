import { AppBar, Link, Toolbar } from "@material-ui/core";
import AddIcon from "@material-ui/icons/Add";
import classnames from "classnames";
import React, { FunctionComponent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useLocation } from "react-router-dom";
import { ReactComponent as ThirdEye } from "../../../assets/icons/third-eye.svg";
import { isAuthenticated } from "../../utils/auth/auth.util";
import {
    AppRoute,
    getAlertsCreatePath,
    getAlertsPath,
    getAnomaliesAllPath,
    getBasePath,
    getHomePath,
    getSignInPath,
    getSignOutPath,
} from "../../utils/route/routes.util";
import { Button } from "../button/button.component";
import { applicationBarStyles } from "./application-bar.styles";

export const ApplicationBar: FunctionComponent = () => {
    const applicationBarClasses = applicationBarStyles();

    const [authenticated] = useState(isAuthenticated());
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
        <AppBar className={applicationBarClasses.applicationBar}>
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
                        isRouteCurrent(AppRoute.HOME)
                            ? applicationBarClasses.selected
                            : applicationBarClasses.unSelected
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
                        isRouteCurrent(AppRoute.ALERTS)
                            ? applicationBarClasses.selected
                            : applicationBarClasses.unSelected
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
                        isRouteCurrent(AppRoute.ANOMALIES)
                            ? applicationBarClasses.selected
                            : applicationBarClasses.unSelected
                    )}
                    component="button"
                    variant="subtitle2"
                    onClick={onAnomaliesClick}
                >
                    {t("label.anomalies")}
                </Link>

                {/* Create alert */}
                <Button
                    className={classnames(
                        applicationBarClasses.link,
                        applicationBarClasses.rightAlign
                    )}
                    color="primary"
                    startIcon={<AddIcon />}
                    variant="outlined"
                    onClick={onCreateAlert}
                >
                    {t("label.create-alert")}
                </Button>

                {(authenticated && (
                    // Sign out
                    <Link
                        className={classnames(
                            applicationBarClasses.link,
                            isRouteCurrent(AppRoute.SIGN_OUT)
                                ? applicationBarClasses.selected
                                : applicationBarClasses.unSelected
                        )}
                        component="button"
                        variant="subtitle2"
                        onClick={onSignOutClick}
                    >
                        {t("label.sign-out")}
                    </Link>
                )) || (
                    // Sign in
                    <Link
                        className={classnames(
                            applicationBarClasses.link,
                            isRouteCurrent(AppRoute.SIGN_IN)
                                ? applicationBarClasses.selected
                                : applicationBarClasses.unSelected
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
