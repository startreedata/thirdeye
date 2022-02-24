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
    getDatasetsPath,
    getDatasourcesPath,
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
                <PageHeaderTabV1 href={getSubscriptionGroupsPath()}>
                    {t("label.subscription-groups")}
                </PageHeaderTabV1>
                <PageHeaderTabV1 href={getDatasetsPath()}>
                    {t("label.datasets")}
                </PageHeaderTabV1>
                <PageHeaderTabV1 href={getDatasourcesPath()}>
                    {t("label.datasources")}
                </PageHeaderTabV1>
                <PageHeaderTabV1 href={getMetricsPath()}>
                    {t("label.metrics")}
                </PageHeaderTabV1>
            </PageHeaderTabsV1>
        </PageHeaderV1>
    );
};
