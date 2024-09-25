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
import { AxiosError } from "axios/index";
import React, { FunctionComponent, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { validateSubscriptionGroup } from "../../components/subscription-group-wizard/subscription-group-wizard.utils";
import { useNotificationProviderV1 } from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { EditableAlert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { createSubscriptionGroup } from "../../rest/subscription-groups/subscription-groups.rest";
import { handleCreateAlertClickGenerator } from "../../utils/anomalies/anomalies.util";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import { getAlertsAlertPath } from "../../utils/routes/routes.util";
import { createEmptySubscriptionGroup } from "../../utils/subscription-groups/subscription-groups.util";
import { AlertsEditCreateBasePageComponent } from "../alerts-edit-create-common/alerts-edit-create-base-page.component";
import { QUERY_PARAM_KEY_ANOMALIES_RETRY } from "../alerts-view-page/alerts-view-page.utils";
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
    const [isEditRequestInFlight, setIsEditRequestInFlight] = useState(false);

    const [singleNewSubscriptionGroup, setSingleNewSubscriptionGroup] =
        useState<SubscriptionGroup>(createEmptySubscriptionGroup());

    const createAlertAndUpdateSubscriptionGroups = useMemo(() => {
        return handleCreateAlertClickGenerator(notify, t, (savedAlert) => {
            const searchParams = new URLSearchParams([
                [QUERY_PARAM_KEY_ANOMALIES_RETRY, "true"],
            ]);
            navigate(getAlertsAlertPath(savedAlert.id, searchParams));
        });
    }, [navigate, notify, t]);

    const handleCreateAlertClick = async (
        alert: EditableAlert
    ): Promise<void> => {
        if (
            validateSubscriptionGroup(singleNewSubscriptionGroup) &&
            singleNewSubscriptionGroup.specs?.length > 0
        ) {
            try {
                setIsEditRequestInFlight(true);
                const newlyCreatedSubGroup = await createSubscriptionGroup(
                    singleNewSubscriptionGroup
                );
                // If creating new subscription group is successful, add it
                // to the list of subscription groups to assign alert to
                createAlertAndUpdateSubscriptionGroups(alert, [
                    ...subscriptionGroups,
                    newlyCreatedSubGroup,
                ]);
            } catch (error) {
                notifyIfErrors(
                    ActionStatus.Error,
                    getErrorMessages(error as AxiosError),
                    notify,
                    t(
                        "message.experienced-error-creating-subscription-group-while-creating-alert"
                    )
                );
            } finally {
                setIsEditRequestInFlight(false);
            }
        } else {
            createAlertAndUpdateSubscriptionGroups(alert, subscriptionGroups);
        }
    };

    return (
        <AlertsEditCreateBasePageComponent
            isEditRequestInFlight={isEditRequestInFlight}
            newSubscriptionGroup={singleNewSubscriptionGroup}
            pageTitle={t("label.create-entity", {
                entity: t("label.alert"),
            })}
            selectedSubscriptionGroups={subscriptionGroups}
            startingAlertConfiguration={startingAlertConfiguration}
            onNewSubscriptionGroupChange={setSingleNewSubscriptionGroup}
            onSubmit={handleCreateAlertClick}
            onSubscriptionGroupChange={setSubscriptionGroups}
        />
    );
};
