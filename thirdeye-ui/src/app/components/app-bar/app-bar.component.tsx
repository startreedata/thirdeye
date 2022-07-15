/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import { Description, ExitToApp, Home, LockOpen } from "@material-ui/icons";
import ErrorIcon from "@material-ui/icons/Error";
import SettingsIcon from "@material-ui/icons/Settings";
import WifiTetheringIcon from "@material-ui/icons/WifiTethering";
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
import { THIRDEYE_DOC_LINK } from "../../utils/constants/constants.util";
import {
    AppRoute,
    getAlertsPath,
    getAnomaliesAllPath,
    getBasePath,
    getConfigurationPath,
    getLoginPath,
    getLogoutPath,
} from "../../utils/routes/routes.util";
import { AboutDialogNavBarButton } from "../about-dialog-nav-bar-button/about-dialog-nav-bar-button.component";

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
                        <WifiTetheringIcon />
                    </NavBarLinkIconV1>
                    <NavBarLinkTextV1>{t("label.alerts")}</NavBarLinkTextV1>
                </NavBarLinkV1>

                {/* Anomalies */}
                <NavBarLinkV1
                    href={getAnomaliesAllPath()}
                    selected={isRouteCurrent(AppRoute.ANOMALIES)}
                >
                    <NavBarLinkIconV1>
                        <ErrorIcon />
                    </NavBarLinkIconV1>
                    <NavBarLinkTextV1>{t("label.anomalies")}</NavBarLinkTextV1>
                </NavBarLinkV1>

                {/* Configuration */}
                <NavBarLinkV1
                    href={getConfigurationPath()}
                    selected={isRouteCurrent(AppRoute.CONFIGURATION)}
                >
                    <NavBarLinkIconV1>
                        <SettingsIcon />
                    </NavBarLinkIconV1>
                    <NavBarLinkTextV1>
                        {t("label.configuration")}
                    </NavBarLinkTextV1>
                </NavBarLinkV1>
            </NavBarPrimaryContainerV1>
            <NavBarSecondaryContainerV1>
                {/* About */}
                <AboutDialogNavBarButton />
                {/* Documentation */}
                <NavBarLinkV1
                    externalLink
                    href={THIRDEYE_DOC_LINK}
                    target="_blank"
                >
                    <NavBarLinkIconV1>
                        <Description />
                    </NavBarLinkIconV1>
                    <NavBarLinkTextV1>
                        {t("label.documentation")}
                    </NavBarLinkTextV1>
                </NavBarLinkV1>

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
