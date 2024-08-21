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
import { DataProps, DetectionData } from "./interfaces";

export const useGetData = ({
    selectedAlert,
    defaultAlertDropdownOption,
    allAnomalies,
    allPreviousPeriodAnomalies,
    currentPeriodAnomaliesByAlert,
    previousPeriodAnomaliesByAlert,
}: DataProps): DetectionData => {
    const [anomalies, setAnomalies] = useState(allAnomalies);
    const [previousPeriodAnomalies, setPreviousPeriodAnomalies] = useState(
        allPreviousPeriodAnomalies
    );
    useEffect(() => {
        if (selectedAlert === defaultAlertDropdownOption) {
            setAnomalies(allAnomalies);
        }
    }, [selectedAlert, allAnomalies]);

    useEffect(() => {
        if (selectedAlert === defaultAlertDropdownOption) {
            setPreviousPeriodAnomalies(allPreviousPeriodAnomalies);
        }
    }, [selectedAlert, allPreviousPeriodAnomalies]);

    useEffect(() => {
        setAnomalies(currentPeriodAnomaliesByAlert);
        setPreviousPeriodAnomalies(previousPeriodAnomaliesByAlert);
    }, [currentPeriodAnomaliesByAlert, previousPeriodAnomaliesByAlert]);

    return { anomalies, previousPeriodAnomalies };
};
