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
import { isEmpty } from "lodash";
import React, { FunctionComponent, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { validateSubscriptionGroup } from "../../components/subscription-group-wizard/subscription-group-whizard.utils";
import {
    NotificationTypeV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { EditableAlert } from "../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../rest/dto/subscription-group.interfaces";
import { createSubscriptionGroup } from "../../rest/subscription-groups/subscription-groups.rest";
import { handleCreateAlertClickGenerator } from "../../utils/anomalies/anomalies.util";
import { getErrorMessages } from "../../utils/rest/rest.util";
import { getAlertsAlertPath } from "../../utils/routes/routes.util";
import { createEmptySubscriptionGroup } from "../../utils/subscription-groups/subscription-groups.util";
import { AlertsEditBasePage } from "../alerts-update-page/alerts-edit-base-page.component";
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
                const errMessages = getErrorMessages(error);

                notify(
                    NotificationTypeV1.Error,
                    t(
                        "message.experienced-error-creating-subscription-group-while-creating-alert"
                    )
                );
                !isEmpty(errMessages) &&
                    errMessages.map((err) =>
                        notify(NotificationTypeV1.Error, err)
                    );
            }
        } else {
            createAlertAndUpdateSubscriptionGroups(alert, subscriptionGroups);
        }
    };

    return (
        <AlertsEditBasePage
            newSubscriptionGroup={singleNewSubscriptionGroup}
            pageTitle={t("label.create-entity", {
                entity: t("label.alert"),
            })}
            selectedSubscriptionGroups={subscriptionGroups}
            startingAlertConfiguration={startingAlertConfiguration}
            submitButtonLabel={t("label.create-entity", {
                entity: t("label.alert"),
            })}
            onNewSubscriptionGroupChange={setSingleNewSubscriptionGroup}
            onSubmit={handleCreateAlertClick}
            onSubscriptionGroupChange={setSubscriptionGroups}
        />
    );
};
