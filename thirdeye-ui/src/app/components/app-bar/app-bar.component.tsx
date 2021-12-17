import { ExitToApp, Home, LockOpen } from "@material-ui/icons";
import {
    NavBarLinkIconV1,
    NavBarLinkTextV1,
    NavBarLinkV1,
    NavBarPrimaryContainerV1,
    NavBarSecondaryContainerV1,
    NavBarV1,
    useAuthProviderV1,
} from "@startree-ui/platform-ui";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { useLocation } from "react-router-dom";
import { ReactComponent as AlertIcon } from "../../../assets/images/alert.svg";
import { ReactComponent as AnomalyIcon } from "../../../assets/images/anomaly.svg";
import { ReactComponent as ConfigurationIcon } from "../../../assets/images/configuration.svg";
import {
    AppRoute,
    getAlertsPath,
    getAnomaliesAllPath,
    getBasePath,
    getConfigurationPath,
    getLoginPath,
    getLogoutPath,
} from "../../utils/routes/routes.util";

export const AppBar: FunctionComponent = () => {
    const { authDisabled, authenticated } = useAuthProviderV1();
    const location = useLocation();
    const { t } = useTranslation();

    const isRouteCurrent = (route: string): boolean => {
        return location.pathname.indexOf(route) === 0;
    };

    return (
        <NavBarV1
            maximizeLabel={t("label.maximize")}
            minimizeLabel={t("label.minimize")}
        >
            <NavBarPrimaryContainerV1>
                {/* Home */}
                <NavBarLinkV1
                    href={getBasePath()}
                    selected={isRouteCurrent(AppRoute.HOME)}
                >
                    <NavBarLinkIconV1>
                        <Home />
                    </NavBarLinkIconV1>
                    <NavBarLinkTextV1>{t("label.home")}</NavBarLinkTextV1>
                </NavBarLinkV1>

                {/* Alerts */}
                <NavBarLinkV1
                    href={getAlertsPath()}
                    selected={isRouteCurrent(AppRoute.ALERTS)}
                >
                    <NavBarLinkIconV1>
                        <AlertIcon />
                    </NavBarLinkIconV1>
                    <NavBarLinkTextV1>{t("label.alerts")}</NavBarLinkTextV1>
                </NavBarLinkV1>

                {/* Anomalies */}
                <NavBarLinkV1
                    href={getAnomaliesAllPath()}
                    selected={isRouteCurrent(AppRoute.ANOMALIES)}
                >
                    <NavBarLinkIconV1>
                        <AnomalyIcon />
                    </NavBarLinkIconV1>
                    <NavBarLinkTextV1>{t("label.anomalies")}</NavBarLinkTextV1>
                </NavBarLinkV1>

                {/* Configuration */}
                <NavBarLinkV1
                    href={getConfigurationPath()}
                    selected={isRouteCurrent(AppRoute.CONFIGURATION)}
                >
                    <NavBarLinkIconV1>
                        <ConfigurationIcon />
                    </NavBarLinkIconV1>
                    <NavBarLinkTextV1>
                        {t("label.configuration")}
                    </NavBarLinkTextV1>
                </NavBarLinkV1>
            </NavBarPrimaryContainerV1>
            <NavBarSecondaryContainerV1>
                {/* Login */}
                {!authenticated && !authDisabled && (
                    <NavBarLinkV1
                        href={getLoginPath()}
                        selected={isRouteCurrent(AppRoute.LOGIN)}
                    >
                        <NavBarLinkIconV1>
                            <LockOpen />
                        </NavBarLinkIconV1>

                        <NavBarLinkTextV1>{t("label.login")}</NavBarLinkTextV1>
                    </NavBarLinkV1>
                )}

                {/* Logout */}
                {authenticated && !authDisabled && (
                    <NavBarLinkV1
                        href={getLogoutPath()}
                        selected={isRouteCurrent(AppRoute.LOGIN)}
                    >
                        <NavBarLinkIconV1>
                            <ExitToApp />
                        </NavBarLinkIconV1>

                        <NavBarLinkTextV1>{t("label.logout")}</NavBarLinkTextV1>
                    </NavBarLinkV1>
                )}
            </NavBarSecondaryContainerV1>
        </NavBarV1>
    );
};
