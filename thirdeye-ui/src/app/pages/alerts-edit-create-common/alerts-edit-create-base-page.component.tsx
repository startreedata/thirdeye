/*
 * Copyright 2023 StarTree Inc
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
import { Box, Typography } from "@material-ui/core";
import { isEqual } from "lodash";
import React, { FunctionComponent } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { createNewStartingAlert } from "../../components/alert-wizard-v2/alert-template/alert-template.utils";
import { PageV1, useDialogProviderV1 } from "../../platform/components";
import { DialogType } from "../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { getAlertsAllPath } from "../../utils/routes/routes.util";
import {
    AlertsEditCreateBasePageComponentProps,
    BOTTOM_BAR_ELEMENT_ID,
} from "./alerts-edit-create-common-page.interfaces";
import { AlertsSimpleAdvancedJsonContainerPage } from "./alerts-simple-advanced-json-container-page.component";

export const AlertsEditCreateBasePageComponent: FunctionComponent<AlertsEditCreateBasePageComponentProps> =
    ({
        isEditRequestInFlight,
        startingAlertConfiguration,
        onSubmit,
        selectedSubscriptionGroups,
        onSubscriptionGroupChange,
        newSubscriptionGroup,
        onNewSubscriptionGroupChange,
    }) => {
        const navigate = useNavigate();
        const { showDialog } = useDialogProviderV1();
        const { t } = useTranslation();

        /**
         * Prompt the user if they are sure they want to leave
         */
        const handlePageExitChecks = (): void => {
            // If user has not input anything navigate to all alerts page
            if (isEqual(alert, createNewStartingAlert())) {
                navigate(getAlertsAllPath());
            } else {
                showDialog({
                    type: DialogType.ALERT,
                    headerText: t("message.redirected-to-another-page"),
                    contents: (
                        <>
                            <Typography variant="body1">
                                <strong>
                                    {t(
                                        "message.do-you-want-to-leave-this-page"
                                    )}
                                </strong>
                            </Typography>
                            <ul>
                                <li>{t("message.your-changes-wont-save")}</li>
                            </ul>
                        </>
                    ),
                    okButtonText: t("label.yes-leave-page"),
                    cancelButtonText: t("label.no-stay"),
                    onOk: () => {
                        navigate(getAlertsAllPath());
                    },
                });
            }
        };

        return (
            <PageV1>
                <AlertsSimpleAdvancedJsonContainerPage
                    isEditRequestInFlight={isEditRequestInFlight}
                    newSubscriptionGroup={newSubscriptionGroup}
                    selectedSubscriptionGroups={selectedSubscriptionGroups}
                    startingAlertConfiguration={startingAlertConfiguration}
                    onNewSubscriptionGroupChange={onNewSubscriptionGroupChange}
                    onPageExit={handlePageExitChecks}
                    onSubmit={onSubmit}
                    onSubscriptionGroupChange={onSubscriptionGroupChange}
                />

                <Box
                    bottom={0}
                    id={BOTTOM_BAR_ELEMENT_ID}
                    marginTop="auto"
                    position="sticky"
                    width="100%"
                />
            </PageV1>
        );
    };
