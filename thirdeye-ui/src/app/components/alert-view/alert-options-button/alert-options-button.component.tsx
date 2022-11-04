// Copyright 2022 StarTree Inc

// Licensed under the StarTree Community License (the "License"); you may not use
// this file except in compliance with the License. You may obtain a copy of the
// License at http://www.startree.ai/legal/startree-community-license

// Unless required by applicable law or agreed to in writing, software distributed under the
// License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
// either express or implied.
// See the License for the specific language governing permissions and limitations under
// the License.
import { IconButton, Menu, MenuItem } from "@material-ui/core";
import MoreVertIcon from "@material-ui/icons/MoreVert";
import React, { FunctionComponent, MouseEvent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import {
    getAlertsAlertPath,
    getAlertsCreateCopyPath,
    getAlertsUpdatePath,
} from "../../../utils/routes/routes.util";
import { AlertOptionsButtonProps } from "./alert-options-button.interfaces";

export const AlertOptionsButton: FunctionComponent<AlertOptionsButtonProps> = ({
    alert,
    onChange,
    onDelete,
    onReset,
    showViewDetails,
    openButtonRenderer,
}) => {
    const [alertOptionsAnchorElement, setAlertOptionsAnchorElement] =
        useState<HTMLElement | null>();
    const navigate = useNavigate();
    const { t } = useTranslation();

    const handleAlertOptionsClick = (event: MouseEvent<HTMLElement>): void => {
        setAlertOptionsAnchorElement(event.currentTarget);
    };

    const handleAlertOptionsClose = (): void => {
        setAlertOptionsAnchorElement(null);
    };

    const handleAlertViewDetails = (): void => {
        if (!alert) {
            return;
        }

        navigate(getAlertsAlertPath(alert.id));
        handleAlertOptionsClose();
    };

    const handleAlertStateToggle = (): void => {
        if (!alert || !alert) {
            return;
        }

        alert.active = !alert.active;
        onChange && onChange(alert);
        handleAlertOptionsClose();
    };

    const handleAlertDuplicate = (): void => {
        if (!alert) {
            return;
        }

        navigate(getAlertsCreateCopyPath(alert.id));
        handleAlertOptionsClose();
    };

    const handleAlertEdit = (): void => {
        if (!alert) {
            return;
        }

        navigate(getAlertsUpdatePath(alert.id));
        handleAlertOptionsClose();
    };

    const handleAlertDelete = (): void => {
        if (!alert) {
            return;
        }

        onDelete && onDelete(alert);
        handleAlertOptionsClose();
    };

    const handleReset = (): void => {
        if (!alert) {
            return;
        }

        onReset && onReset(alert);
        handleAlertOptionsClose();
    };

    return (
        <>
            {/* Alert options button */}
            {openButtonRenderer && openButtonRenderer(handleAlertOptionsClick)}
            {!openButtonRenderer && (
                <IconButton color="secondary" onClick={handleAlertOptionsClick}>
                    <MoreVertIcon />
                </IconButton>
            )}

            {/* Alert options */}
            <Menu
                anchorEl={alertOptionsAnchorElement}
                open={Boolean(alertOptionsAnchorElement)}
                onClose={handleAlertOptionsClose}
            >
                {/* View details */}
                {showViewDetails && (
                    <MenuItem onClick={handleAlertViewDetails}>
                        {t("label.view-details")}
                    </MenuItem>
                )}

                {/* Activate/deactivate alert */}
                <MenuItem onClick={handleAlertStateToggle}>
                    {alert.active
                        ? t("label.deactivate-entity", {
                              entity: t("label.alert"),
                          })
                        : t("label.activate-entity", {
                              entity: t("label.alert"),
                          })}
                </MenuItem>

                {/* Duplicate alert */}
                <MenuItem onClick={handleAlertDuplicate}>
                    {t("label.duplicate-entity", {
                        entity: t("label.alert"),
                    })}
                </MenuItem>

                {/* Edit alert */}
                <MenuItem onClick={handleAlertEdit}>
                    {t("label.edit-entity", {
                        entity: t("label.alert"),
                    })}
                </MenuItem>

                {/* Delete alert */}
                <MenuItem onClick={handleAlertDelete}>
                    {t("label.delete-entity", {
                        entity: t("label.alert"),
                    })}
                </MenuItem>

                {/* Reset alert */}
                <MenuItem onClick={handleReset}>
                    {t("label.reset-anomalies-for-alert")}
                </MenuItem>
            </Menu>
        </>
    );
};
