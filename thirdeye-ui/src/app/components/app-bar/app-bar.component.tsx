import { Menu, MenuItem } from "@material-ui/core";
import { Add, ExitToApp, Home, LockOpen } from "@material-ui/icons";
import {
    NavBarLinkIconV1,
    NavBarLinkTextV1,
    NavBarLinkV1,
    NavBarPrimaryContainerV1,
    NavBarSecondaryContainerV1,
    NavBarV1,
} from "@startree-ui/platform-ui";
import React, { FunctionComponent, MouseEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useHistory, useLocation } from "react-router-dom";
import { ReactComponent as AlertIcon } from "../../../assets/images/alert.svg";
import { ReactComponent as AnomalyIcon } from "../../../assets/images/anomaly.svg";
import { ReactComponent as ConfigurationIcon } from "../../../assets/images/configuration.svg";
import {
    AppRoute,
    getAlertsCreatePath,
    getAlertsPath,
    getAnomaliesAllPath,
    getBasePath,
    getConfigurationPath,
    getDatasetsOnboardPath,
    getDatasourcesCreatePath,
    getLoginPath,
    getLogoutPath,
    getMetricsCreatePath,
    getSubscriptionGroupsCreatePath,
} from "../../utils/routes/routes.util";
import { useAuth } from "../auth-provider/auth-provider.component";

export const AppBar: FunctionComponent = () => {
    const [
        shortcutOptionsAnchorElement,
        setShortcutOptionsAnchorElement,
    ] = useState<HTMLElement | null>();

    const { authDisabled, authenticated } = useAuth();
    const history = useHistory();
    const location = useLocation();
    const { t } = useTranslation();

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

    const handleCreateMetric = (): void => {
        history.push(getMetricsCreatePath());
        handleShortcutOptionsClose();
    };

    const handleOnBoardDataset = (): void => {
        history.push(getDatasetsOnboardPath());
        handleShortcutOptionsClose();
    };

    const handleCreateDatasource = (): void => {
        history.push(getDatasourcesCreatePath());
        handleShortcutOptionsClose();
    };

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

                {(authDisabled || authenticated) && (
                    <>
                        <span onClick={handleShortcutOptionsClick}>
                            <NavBarLinkV1>
                                <NavBarLinkIconV1>
                                    <Add />
                                </NavBarLinkIconV1>
                                <NavBarLinkTextV1>Add New</NavBarLinkTextV1>
                            </NavBarLinkV1>
                        </span>

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

                            {/* Create metric */}
                            <MenuItem onClick={handleCreateMetric}>
                                {t("label.create-entity", {
                                    entity: t("label.metric"),
                                })}
                            </MenuItem>

                            {/* Create dataset */}
                            <MenuItem onClick={handleOnBoardDataset}>
                                {t("label.onboard-entity", {
                                    entity: t("label.dataset"),
                                })}
                            </MenuItem>

                            {/* Create datasource */}
                            <MenuItem onClick={handleCreateDatasource}>
                                {t("label.create-entity", {
                                    entity: t("label.datasource"),
                                })}
                            </MenuItem>
                        </Menu>
                    </>
                )}
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
