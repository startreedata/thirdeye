/*
 * Copyright 2024 StarTree Inc
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
import { useEffect, useState } from "react";

// Interfaces
import { APIRequestData, ApiRequestsProps } from "./api-requests.interfaces";

// APIs
import { useGetSubscriptionGroups } from "../../../rest/subscription-groups/subscription-groups.actions";
import {
    useGetAnomalies,
    useGetAnomaly,
} from "../../../rest/anomalies/anomaly.actions";
import { useGetInvestigations } from "../../../rest/rca/rca.actions";

// Utils
import {
    anaylysisPeriodPreviousWindowTimeMapping,
    anaylysisPeriodStartTimeMapping,
} from "../../../platform/utils";
import { notifyIfErrors } from "../../../utils/notifications/notifications.util";
import { useGetAlertsCount } from "../../../rest/alerts/alerts.actions";

import { useNotificationProviderV1 } from "../../../platform/components";
import { useTranslation } from "react-i18next";
import { isEmpty } from "lodash";

export const useApiRequests = ({
    selectedAnalysisPeriod,
}: ApiRequestsProps): APIRequestData => {
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();
    const [recentAnomalyInvestigatedId, setRecentAnomalyInvestigatedId] =
        useState<number>();
    const {
        subscriptionGroups,
        getSubscriptionGroups,
        status: subscriptionGroupsStatus,
        errorMessages: subscriptionGroupsErrorMessages,
    } = useGetSubscriptionGroups();

    const {
        anomaly: mostRecentlyInvestigatedAnomaly,
        getAnomaly,
        status: anomalyStatus,
        errorMessages: anomalyErrorMessages,
    } = useGetAnomaly();

    const {
        alertsCount,
        getAlertsCount,
        status: alertsCountStatus,
        errorMessages: alertsCountErrorMessages,
    } = useGetAlertsCount();
    const {
        anomalies,
        getAnomalies,
        status: anomaliesStatus,
        errorMessages: anomaliesErrorMessages,
    } = useGetAnomalies();

    const {
        anomalies: previousPeriodAnomalies,
        getAnomalies: getPreviousPeriodAnomalies,
        status: previousPeriodAnomaliesStatus,
        errorMessages: previousPeriodAnomaliesErrorMessages,
    } = useGetAnomalies();

    const {
        investigations,
        getInvestigations,
        status: investigationStatus,
        errorMessages: investigationErrorMessages,
    } = useGetInvestigations();

    useEffect(() => {
        notifyIfErrors(
            anomalyStatus,
            anomalyErrorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.anomaly"),
            })
        );
    }, [anomalyStatus]);

    useEffect(() => {
        notifyIfErrors(
            subscriptionGroupsStatus,
            subscriptionGroupsErrorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.subscription-groups"),
            })
        );
    }, [subscriptionGroupsStatus]);

    useEffect(() => {
        notifyIfErrors(
            alertsCountStatus,
            alertsCountErrorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.alerts-count"),
            })
        );
    }, [alertsCountStatus]);

    useEffect(() => {
        notifyIfErrors(
            anomaliesStatus,
            anomaliesErrorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.anomalies"),
            })
        );
    }, [anomaliesStatus]);

    useEffect(() => {
        notifyIfErrors(
            previousPeriodAnomaliesStatus,
            previousPeriodAnomaliesErrorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: "Previous period anomalies",
            })
        );
    }, [previousPeriodAnomaliesStatus]);

    useEffect(() => {
        notifyIfErrors(
            investigationStatus,
            investigationErrorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.investigation"),
            })
        );
    }, [investigationStatus]);

    useEffect(() => {
        if (selectedAnalysisPeriod) {
            const currentPeriod =
                anaylysisPeriodStartTimeMapping[selectedAnalysisPeriod];
            const previousPeriod =
                anaylysisPeriodPreviousWindowTimeMapping[
                    selectedAnalysisPeriod
                ];
            getAnomalies({ startTime: currentPeriod.startTime });
            getPreviousPeriodAnomalies({
                startTime: previousPeriod.startTime,
                endTime: previousPeriod.endTime,
            });
            getInvestigations(undefined, currentPeriod.startTime);
            getAlertsCount();
            getSubscriptionGroups();
        }
    }, [selectedAnalysisPeriod]);

    useEffect(() => {
        if (!isEmpty(investigations)) {
            const recentInvestigation = investigations?.sort(
                (a, b) => b.created - a.created
            )[0];
            setRecentAnomalyInvestigatedId(recentInvestigation?.anomaly?.id);
        }
    }, [investigations]);

    useEffect(() => {
        if (recentAnomalyInvestigatedId) {
            getAnomaly(recentAnomalyInvestigatedId);
        }
    }, [recentAnomalyInvestigatedId]);

    return {
        anomalies,
        previousPeriodAnomalies,
        investigations,
        alertsCount,
        subscriptionGroups,
        mostRecentlyInvestigatedAnomalyAlert:
            mostRecentlyInvestigatedAnomaly?.alert,
    };
};
