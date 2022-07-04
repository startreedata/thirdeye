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
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import {
    PageHeaderActionsV1,
    PageHeaderTabsV1,
    PageHeaderTabV1,
    PageHeaderTextV1,
    PageHeaderV1,
} from "../../platform/components";
import {
    getAlertTemplatesPath,
    getDatasetsPath,
    getDatasourcesPath,
    getEventsAllPath,
    getMetricsPath,
    getSubscriptionGroupsPath,
} from "../../utils/routes/routes.util";
import { CreateMenuButton } from "../create-menu-button.component/create-menu-button.component";
import { ConfigurationPageHeaderProps } from "./configuration-page-header.interfaces";

export const ConfigurationPageHeader: FunctionComponent<
    ConfigurationPageHeaderProps
> = (props: ConfigurationPageHeaderProps) => {
    const { t } = useTranslation();

    return (
        <PageHeaderV1>
            <PageHeaderTextV1>{t("label.configuration")}</PageHeaderTextV1>
            <PageHeaderActionsV1>
                {/* Create options button */}
                <CreateMenuButton />
            </PageHeaderActionsV1>

            <PageHeaderTabsV1 selectedIndex={props.selectedIndex}>
                <PageHeaderTabV1 href={getDatasourcesPath()}>
                    {t("label.datasources")}
                </PageHeaderTabV1>
                <PageHeaderTabV1 href={getDatasetsPath()}>
                    {t("label.datasets")}
                </PageHeaderTabV1>
                <PageHeaderTabV1 href={getMetricsPath()}>
                    {t("label.metrics")}
                </PageHeaderTabV1>
                <PageHeaderTabV1 href={getAlertTemplatesPath()}>
                    {t("label.alert-templates")}
                </PageHeaderTabV1>
                <PageHeaderTabV1 href={getSubscriptionGroupsPath()}>
                    {t("label.subscription-groups")}
                </PageHeaderTabV1>
                <PageHeaderTabV1 href={getEventsAllPath()}>
                    {t("label.events")}
                </PageHeaderTabV1>
            </PageHeaderTabsV1>
        </PageHeaderV1>
    );
};
