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
import { AxiosError } from "axios";
import { getAllAlerts } from "../../../../../../rest/alerts/alerts.rest";
import { useFetchQuery } from "../../../../../../rest/hooks/useFetchQuery";
import { Alert } from "../../../../../../rest/dto/alert.interfaces";
import { SubscriptionGroup } from "../../../../../../rest/dto/subscription-group.interfaces";
import { getAllSubscriptionGroups } from "../../../../../../rest/subscription-groups/subscription-groups.rest";
import { useEffect } from "react";
import { notifyIfErrors } from "../../../../../../utils/notifications/notifications.util";
import { ActionStatus } from "../../../../../../rest/actions.interfaces";
import { getErrorMessages } from "../../../../../../utils/rest/rest.util";
import { useTranslation } from "react-i18next";
import {
    NotificationTypeV1,
    useNotificationProviderV1,
} from "../../../../../../platform/components";
import { useResetAlert } from "../../../../../../rest/alerts/alerts.actions";

type AlertApiRequest = {
    alerts: Alert[] | undefined;
    subscriptionGroups: SubscriptionGroup[] | undefined;
    error: boolean;
    loading: boolean;
    resetAlert: (id: number) => Promise<Alert | undefined>;
};

export const useAlertApiRequest = (): AlertApiRequest => {
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();
    const {
        data: alerts,
        isInitialLoading: isGetAlertLoading,
        isError: isGetAlertError,
        error: getAlertsErrors,
    } = useFetchQuery<Alert[], AxiosError>({
        queryKey: ["alerts"],
        queryFn: () => getAllAlerts(),
    });

    const {
        data: subscriptionGroups,
        isInitialLoading: isGetSubscriptionGroupsLoading,
        isError: isGetSubscriptionGroupsError,
        error: getSubscriptionGroupsErrors,
    } = useFetchQuery<SubscriptionGroup[], AxiosError>({
        queryKey: ["subscriptiongroups"],
        queryFn: () => getAllSubscriptionGroups(),
    });

    const {
        alert: alertThatWasReset,
        resetAlert,
        status,
        errorMessages,
    } = useResetAlert();

    useEffect(() => {
        if (status === ActionStatus.Done && alertThatWasReset) {
            notify(
                NotificationTypeV1.Success,
                t("message.alert-reset-success", {
                    alertName: alertThatWasReset.name,
                })
            );
        }
        notifyIfErrors(
            status,
            errorMessages,
            notify,
            t("message.alert-reset-error")
        );
    }, [status]);

    useEffect(() => {
        isGetAlertError &&
            notifyIfErrors(
                ActionStatus.Error,
                getErrorMessages(getAlertsErrors),
                notify,
                t("message.error-while-fetching", {
                    entity: t("label.alerts"),
                })
            );
    }, [getAlertsErrors]);

    useEffect(() => {
        isGetSubscriptionGroupsError &&
            notifyIfErrors(
                ActionStatus.Error,
                getErrorMessages(getSubscriptionGroupsErrors),
                notify,
                t("message.error-while-fetching", {
                    entity: t("label.subscription-groups"),
                })
            );
    }, [getSubscriptionGroupsErrors]);

    return {
        alerts,
        subscriptionGroups,
        error: isGetAlertError,
        loading: isGetAlertLoading || isGetSubscriptionGroupsLoading,
        resetAlert,
    };
};
