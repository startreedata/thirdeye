import { Button, Menu, MenuItem } from "@material-ui/core";
import { KeyboardArrowDown } from "@material-ui/icons";
import {
    PageHeaderTabsV1,
    PageHeaderTabV1,
    PageHeaderTextV1,
    PageHeaderV1,
} from "@startree-ui/platform-ui";
import React, { FunctionComponent, MouseEvent } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router";
import {
    AppRoute,
    getAlertsCreatePath,
    getDatasetsOnboardPath,
    getDatasourcesCreatePath,
    getMetricsCreatePath,
    getSubscriptionGroupsCreatePath,
} from "../../utils/routes/routes.util";
import { ConfigurationPageHeaderProps } from "./configuration-page-header.interfaces";

export const ConfigurationPageHeader: FunctionComponent<ConfigurationPageHeaderProps> = (
    props: ConfigurationPageHeaderProps
) => {
    const { t } = useTranslation();
    const [
        shortcutOptionsAnchorElement,
        setShortcutOptionsAnchorElement,
    ] = React.useState<null | HTMLElement>(null);
    const history = useHistory();

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

    return (
        <PageHeaderV1>
            <PageHeaderTextV1>{t("label.configuration")}</PageHeaderTextV1>
            <Button
                color="primary"
                endIcon={<KeyboardArrowDown />}
                variant="contained"
                onClick={handleShortcutOptionsClick}
            >
                Create
            </Button>
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
