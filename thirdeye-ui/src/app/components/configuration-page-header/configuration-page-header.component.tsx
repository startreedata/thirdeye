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
