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
    getAlertTemplatesPath,
    getDatasetsPath,
    getDatasourcesPath,
    getEventsAllPath,
    getMetricsPath,
    getSubscriptionGroupsPath,
} from "../../utils/routes/routes.util";
import { PageHeader } from "../page-header/page-header.component";
import { ConfigurationPageHeaderProps } from "./configuration-page-header.interfaces";

export const ConfigurationPageHeader: FunctionComponent<
    ConfigurationPageHeaderProps
> = ({ selectedIndex }) => {
    const { t } = useTranslation();

    return (
        <PageHeader
            showCreateButton
            subNavigation={[
                {
                    link: getDatasourcesPath(),
                    label: t("label.datasources"),
                },
                {
                    link: getDatasetsPath(),
                    label: t("label.datasets"),
                },
                {
                    link: getMetricsPath(),
                    label: t("label.metrics"),
                },
                {
                    link: getAlertTemplatesPath(),
                    label: t("label.alert-templates"),
                },
                {
                    link: getSubscriptionGroupsPath(),
                    label: t("label.subscription-groups"),
                },
                {
                    link: getEventsAllPath(),
                    label: t("label.events"),
                },
            ]}
            subNavigationSelected={selectedIndex}
            title={t("label.configuration")}
        />
    );
};
