// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import {
    DropdownButtonTypeV1,
    DropdownButtonV1,
} from "../../platform/components";
import {
    getAlertsCreatePath,
    getAlertTemplatesCreatePath,
    getDatasetsOnboardPath,
    getDatasourcesCreatePath,
    getEventsCreatePath,
    getSubscriptionGroupsCreatePath,
} from "../../utils/routes/routes.util";

export const CreateMenuButton: FunctionComponent = () => {
    const { t } = useTranslation();
    const navigate = useNavigate();

    const handleCreateAlert = (): void => {
        navigate(getAlertsCreatePath());
    };

    const handleCreateSubscriptionGroup = (): void => {
        navigate(getSubscriptionGroupsCreatePath());
    };

    const handleOnBoardDataset = (): void => {
        navigate(getDatasetsOnboardPath());
    };

    const handleCreateDatasource = (): void => {
        navigate(getDatasourcesCreatePath());
    };

    const handleCreateAlertTemplate = (): void => {
        navigate(getAlertTemplatesCreatePath());
    };

    const handleCreateEvent = (): void => {
        navigate(getEventsCreatePath());
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
        {
            id: "createEvent",
            text: t("label.create-entity", {
                entity: t("label.event"),
            }),
        },
        {
            id: "createAlertTemplate",
            text: t("label.create-entity", {
                entity: t("label.alert-template"),
            }),
        },
    ];

    const handleShortcutCreateOnclick = (id: number | string): void => {
        switch (id) {
            case "createAlert":
                handleCreateAlert();

                break;
            case "createSubscriptionGroup":
                handleCreateSubscriptionGroup();

                break;

            case "onboardDataset":
                handleOnBoardDataset();

                break;
            case "createDatasource":
                handleCreateDatasource();

                break;
            case "createAlertTemplate":
                handleCreateAlertTemplate();

                break;
            case "createEvent":
                handleCreateEvent();

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
