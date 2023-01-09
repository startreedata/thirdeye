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
import { Box } from "@material-ui/core";
import React, { FunctionComponent, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import {
    useDialogProviderV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { DialogType } from "../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { handleCreateAlertClickGenerator } from "../../utils/anomalies/anomalies.util";
import { getAlertsAlertPath } from "../../utils/routes/routes.util";
import { AlertsEditBasePage } from "../alerts-update-page/alerts-edit-base-page.component";
import { AlertsCreatePageProps } from "./alerts-create-page.interfaces";

export const AlertsCreateBasePage: FunctionComponent<AlertsCreatePageProps> = ({
    startingAlertConfiguration,
}) => {
    const { t } = useTranslation();
    const navigate = useNavigate();
    const { notify } = useNotificationProviderV1();
    const [subscriptionGroups, setSubscriptionGroups] = useState<
        SubscriptionGroup[]
    >([]);

    const { showDialog, hideDialog } = useDialogProviderV1();

    const handleCreateAlertClick = useMemo(() => {
        return handleCreateAlertClickGenerator(notify, t, (savedAlert) => {
            showDialog({
                type: DialogType.CUSTOM,
                contents: (
                    <Box textAlign="center">
                        {t("message.redirecting-to-alert-in-10-seconds")}
                    </Box>
                ),
                hideCancelButton: true,
                hideOkButton: true,
            });

            // Wait 10 seconds so the initial anomalies task run completes
            setTimeout(() => {
                hideDialog();
                navigate(getAlertsAlertPath(savedAlert.id));
            }, 10000);
        });
    }, [navigate, notify, t]);

    return (
        <AlertsEditBasePage
            pageTitle={t("label.create-entity", {
                entity: t("label.alert"),
            })}
            selectedSubscriptionGroups={subscriptionGroups}
            startingAlertConfiguration={startingAlertConfiguration}
            submitButtonLabel={t("label.create-entity", {
                entity: t("label.alert"),
            })}
            onSubmit={(alert) =>
                handleCreateAlertClick(alert, subscriptionGroups)
            }
            onSubscriptionGroupChange={setSubscriptionGroups}
        />
    );
};
