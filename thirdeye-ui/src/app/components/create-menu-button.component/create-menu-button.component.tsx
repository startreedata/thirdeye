import {
    DropdownButtonTypeV1,
    DropdownButtonV1,
} from "@startree-ui/platform-ui";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { useHistory } from "react-router";
import {
    getAlertsCreatePath,
    getDatasetsOnboardPath,
    getDatasourcesCreatePath,
    getMetricsCreatePath,
    getSubscriptionGroupsCreatePath,
} from "../../utils/routes/routes.util";

export const CreateMenuButton: FunctionComponent = () => {
    const { t } = useTranslation();
    const history = useHistory();

    const handleCreateAlert = (): void => {
        history.push(getAlertsCreatePath());
    };

    const handleCreateSubscriptionGroup = (): void => {
        history.push(getSubscriptionGroupsCreatePath());
    };

    const handleCreateMetric = (): void => {
        history.push(getMetricsCreatePath());
    };

    const handleOnBoardDataset = (): void => {
        history.push(getDatasetsOnboardPath());
    };

    const handleCreateDatasource = (): void => {
        history.push(getDatasourcesCreatePath());
    };

    const shortcutCreateMenuItems = [
        {
            id: "createAlert",
            text: t("label.create-entity", {
                entity: t("label.alert"),
            }),
        },
        {
            id: "createSubscriptionGroup",
            text: t("label.create-entity", {
                entity: t("label.subscription-group"),
            }),
        },
        {
            id: "createMetric",
            text: t("label.create-entity", {
                entity: t("label.metric"),
            }),
        },
        {
            id: "onboardDataset",
            text: t("label.onboard-entity", {
                entity: t("label.dataset"),
            }),
        },
        {
            id: "createDatasource",
            text: t("label.create-entity", {
                entity: t("label.datasource"),
            }),
        },
    ];

    const handleShortcutCreateOnclick = (
        id: number | string,
        _: string
    ): void => {
        switch (id) {
            case "createAlert":
                handleCreateAlert();

                break;
            case "createSubscriptionGroup":
                handleCreateSubscriptionGroup();

                break;
            case "createMetric":
                handleCreateMetric();

                break;
            case "onboardDataset":
                handleOnBoardDataset();

                break;
            case "createDatasource":
                handleCreateDatasource();

                break;
            default:
                break;
        }
    };

    return (
        <DropdownButtonV1
            color="primary"
            dropdownMenuItems={shortcutCreateMenuItems}
            type={DropdownButtonTypeV1.Regular}
            onClick={handleShortcutCreateOnclick}
        >
            {t("label.create")}
        </DropdownButtonV1>
    );
};
