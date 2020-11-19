import { AppBar, Button, Link, Toolbar } from "@material-ui/core";
import AddIcon from "@material-ui/icons/Add";
import classnames from "classnames";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useLocation } from "react-router-dom";
import { ReactComponent as ThirdEye } from "../../../assets/icons/third-eye.svg";
import { useAuthStore } from "../../store/auth/auth-store";
import {
    ApplicationRoute,
    getAlertsCreatePath,
    getAlertsPath,
    getAnomaliesAllPath,
    getBasePath,
    getHomePath,
    getSignInPath,
    getSignOutPath,
} from "../../utils/route/routes-util";
import { useApplicationBarStyles } from "./application-bar.styles";

export const ApplicationBar: FunctionComponent = () => {
    const applicationBarClasses = useApplicationBarStyles();
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
                        isRouteCurrent(ApplicationRoute.HOME)
                            ? applicationBarClasses.selected
                            : ""
                    )}
                    component="button"
                    disabled={isRouteCurrent(ApplicationRoute.HOME)}
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
                    disabled={isRouteCurrent(ApplicationRoute.ALERTS)}
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
                    disabled={isRouteCurrent(ApplicationRoute.ANOMALIES)}
                    variant="subtitle2"
                    onClick={onAnomaliesClick}
                >
                    {t("label.anomalies")}
                </Link>

                {(auth && (
                    <>
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

                        {/* Sign out */}
                        <Link
                            className={classnames(
                                applicationBarClasses.link,
                                isRouteCurrent(ApplicationRoute.SIGN_OUT)
                                    ? applicationBarClasses.selected
                                    : ""
                            )}
                            component="button"
                            disabled={isRouteCurrent(ApplicationRoute.SIGN_OUT)}
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
                        disabled={isRouteCurrent(ApplicationRoute.SIGN_IN)}
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
