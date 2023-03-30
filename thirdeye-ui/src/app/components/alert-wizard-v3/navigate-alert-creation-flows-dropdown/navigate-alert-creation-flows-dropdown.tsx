/*
 * Copyright 2022 StarTree Inc
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
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { useLocation, useNavigate } from "react-router-dom";
import {
    DropdownButtonTypeV1,
    DropdownButtonV1,
    DropdownMenuItemV1,
} from "../../../platform/components";
import { AppRouteRelative } from "../../../utils/routes/routes.util";

export const NavigateAlertCreationFlowsDropdown: FunctionComponent = () => {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const location = useLocation();

    /**
     * `/alerts/73/update/simple`
     * `/alerts/create/new/advanced`
     * `/alerts/create/new/new-user/select-type`
     */
    const routePathPrefix = useMemo(() => {
        return location.pathname.split("/").slice(0, 4).join("/");
    }, [location]);

    const shortcutCreateMenuItems: DropdownMenuItemV1[] = [
        {
            id: AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_MONITORING,
            text: t("label.simple"),
        },
        {
            id: AppRouteRelative.ALERTS_CREATE_SIMPLE,
            text: t("label.advanced"),
        },
        {
            id: AppRouteRelative.ALERTS_CREATE_ADVANCED,
            text: t("label.json-editor"),
        },
    ];

    const buttonLabel = useMemo(() => {
        const match = shortcutCreateMenuItems.find((candidate) => {
            return location.pathname.endsWith(candidate.id as string);
        });

        return match ? match.text : t("label.change-editor");
    }, [location]);

    const handleShortcutCreateOnclick = (id: number | string): void => {
        switch (id) {
            case AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_MONITORING:
                navigate(
                    `${routePathPrefix}/${AppRouteRelative.ALERTS_CREATE_NEW_USER}/${AppRouteRelative.WELCOME_CREATE_ALERT_SETUP_MONITORING}`
                );

                break;
            case AppRouteRelative.ALERTS_CREATE_SIMPLE:
                navigate(
                    `${routePathPrefix}/${AppRouteRelative.ALERTS_CREATE_SIMPLE}`
                );

                break;
            case AppRouteRelative.ALERTS_CREATE_ADVANCED:
                navigate(
                    `${routePathPrefix}/${AppRouteRelative.ALERTS_CREATE_ADVANCED}`
                );

                break;
            default:
                break;
        }
    };

    return (
        <>
            {t("label.change-editor")}
            {": "}
            <DropdownButtonV1
                color="primary"
                dropdownMenuItems={shortcutCreateMenuItems}
                type={DropdownButtonTypeV1.Regular}
                onClick={handleShortcutCreateOnclick}
            >
                {buttonLabel}
            </DropdownButtonV1>
        </>
    );
};
