import {
    PageHeaderActionsV1,
    PageHeaderTabsV1,
    PageHeaderTabV1,
    PageHeaderTextV1,
    PageHeaderV1,
} from "@startree-ui/platform-ui";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { AppRoute } from "../../utils/routes/routes.util";
import { CreateMenuButton } from "../create-menu-button.component/create-menu-button.component";
import { ConfigurationPageHeaderProps } from "./configuration-page-header.interfaces";

export const ConfigurationPageHeader: FunctionComponent<ConfigurationPageHeaderProps> = (
    props: ConfigurationPageHeaderProps
) => {
    const { t } = useTranslation();

    return (
        <PageHeaderV1>
            <PageHeaderTextV1>{t("label.configuration")}</PageHeaderTextV1>
            <PageHeaderActionsV1>
                {/* Create options button */}
                <CreateMenuButton />
            </PageHeaderActionsV1>

            <PageHeaderTabsV1 selectedIndex={props.selectedIndex}>
                <PageHeaderTabV1 href={AppRoute.SUBSCRIPTION_GROUPS}>
                    {t("label.subscription-groups")}
                </PageHeaderTabV1>
                <PageHeaderTabV1 href={AppRoute.DATASETS}>
                    {t("label.datasets")}
                </PageHeaderTabV1>
                <PageHeaderTabV1 href={AppRoute.DATASOURCES}>
                    {t("label.datasources")}
                </PageHeaderTabV1>
                <PageHeaderTabV1 href={AppRoute.METRICS}>
                    {t("label.metrics")}
                </PageHeaderTabV1>
            </PageHeaderTabsV1>
        </PageHeaderV1>
    );
};
