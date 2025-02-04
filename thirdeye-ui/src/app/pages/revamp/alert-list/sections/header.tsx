/*
 * Copyright 2025 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 *
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import React from "react";
import { PageHeader } from "../../../../platform/components/layout/page-header/page-header.component";
import {
    DropdownButtonTypeV1,
    DropdownButtonV1,
    DropdownMenuItemV1,
} from "../../../../platform/components";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import {
    getAlertsCeateUpdatedPath,
    getAlertsCreateAdvancePath,
    getAlertsCreateNewJsonEditorPath,
    // getAlertsEasyCreatePath,
} from "../../../../utils/routes/routes.util";

export const Header = (): JSX.Element => {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const createMenuItems: DropdownMenuItemV1[] = [
        {
            id: "easyAlert",
            text: t("label.easy-alert"),
        },
        {
            id: "advancedAlert",
            text: t("label.advanced-alert"),
        },
        {
            id: "jsonAlert",
            text: t("label.json-alert"),
        },
    ];
    const handleAlertRedirect = (alertType: string | number): void => {
        if (alertType === "easyAlert") {
            navigate(getAlertsCeateUpdatedPath());
            // navigate(getAlertsEasyCreatePath());
        } else if (alertType === "advancedAlert") {
            navigate(getAlertsCreateAdvancePath());
        } else if (alertType === "jsonAlert") {
            navigate(getAlertsCreateNewJsonEditorPath());
        }
    };
    const creatAlertButton = (
        <DropdownButtonV1
            color="primary"
            data-testid="create-menu-button"
            dataTestId="create-alert-dropdown"
            dropdownMenuItems={createMenuItems}
            type={DropdownButtonTypeV1.Regular}
            onClick={handleAlertRedirect}
        >
            {t("label.create")}
        </DropdownButtonV1>
    );

    return <PageHeader actionButtons={creatAlertButton} mainHeading="Alerts" />;
};
