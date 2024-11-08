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
import { useEffect } from "react";

// APIs
import { useGetAlerts } from "../../../../rest/alerts/alerts.actions";
import { useGetAnomalies } from "../../../../rest/anomalies/anomaly.actions";

// Utils
import {
    anaylysisPeriodPreviousWindowTimeMapping,
    anaylysisPeriodStartTimeMapping,
} from "../../../../platform/utils";

// Interaces
import { APIRequestData, ApiReuqestProps } from "./api-interfaces";

export const useDetectionPerformanceApiRequests = ({
    selectedAlert,
    defaultAlertDropdownOption,
    selectedAnalysisPeriod,
}: ApiReuqestProps): APIRequestData => {
    const { alerts, getAlerts } = useGetAlerts();
    const {
        anomalies: currentPeriodAnomaliesByAlert,
        getAnomalies: getCurrentPeriodAnomaliesByAlert,
    } = useGetAnomalies();
    const {
        anomalies: previousPeriodAnomaliesByAlert,
        getAnomalies: getPreviousPeriodAnomaliesByAlert,
    } = useGetAnomalies();

    useEffect(() => {
        getAlerts();
    }, []);

    useEffect(() => {
        if (selectedAlert !== defaultAlertDropdownOption) {
            getCurrentPeriodAnomaliesByAlert({
                filterIgnoredAnomalies: true,
                alertId: selectedAlert.id,
                startTime:
                    anaylysisPeriodStartTimeMapping[selectedAnalysisPeriod]
                        .startTime,
            });
            getPreviousPeriodAnomaliesByAlert({
                filterIgnoredAnomalies: true,
                alertId: selectedAlert.id,
                startTime:
                    anaylysisPeriodPreviousWindowTimeMapping[
                        selectedAnalysisPeriod
                    ].startTime,
                endTime:
                    anaylysisPeriodPreviousWindowTimeMapping[
                        selectedAnalysisPeriod
                    ].endTime,
            });
        }
    }, [selectedAnalysisPeriod, selectedAlert]);

    return {
        alerts,
        currentPeriodAnomaliesByAlert,
        previousPeriodAnomaliesByAlert,
    };
};
