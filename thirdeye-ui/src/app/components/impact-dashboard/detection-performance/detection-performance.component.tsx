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
import React, { ReactElement, useEffect, useState } from "react";
import { useStyles } from "./detection-performance.styles";
import { MenuItem, Select, Typography } from "@material-ui/core";
import AnalysisPeriod from "../anaylysis-period/analysis-period.component";
import { DetectionPerformanceProps } from "./detection-performance.interfaces";
import {
    anaylysisPeriodStartTimeMapping,
    anaylysisPeriodPreviousWindowTimeMapping,
} from "../../../platform/utils";
import { useGetAnomalies } from "../../../rest/anomalies/anomaly.actions";
import { useGetAlerts } from "../../../rest/alerts/alerts.actions";
import { TotalAnomaliesGraph } from "./total-anomalies.component";
import { WeekAnomaliesGraph } from "./weekly-anomalies.component";

const defaultAlertDropdownOption = { id: -1, name: "All alerts" };

const DetectionPerformance = ({
    anomalies: allAnomalies,
    previousPeriodAnomalies: allPreviousPeriodAnomalies,
    analysisPeriods,
    selectedAnalysisPeriod,
    onAnalysisPeriodChange,
}: DetectionPerformanceProps): ReactElement => {
    const { alerts, getAlerts, status, errorMessages } = useGetAlerts();
    const {
        anomalies: currentPeriodAnomaliesByAlert,
        getAnomalies: getCurrentPeriodAnomaliesByAlert,
    } = useGetAnomalies();
    const {
        anomalies: previousPeriodAnomaliesByAlert,
        getAnomalies: getPreviousPeriodAnomaliesByAlert,
    } = useGetAnomalies();
    const [alertDropdownOptions, setAlertDropdownOptions] =
        useState<{ id: number; name: string }[]>();
    const [anomalies, setAnomalies] = useState(allAnomalies);
    const [previousPeriodAnomalies, setPreviousPeriodAnomalies] = useState(
        allPreviousPeriodAnomalies
    );
    const [selectedAlert, setSelectedAlert] = useState<{
        id: number;
        name: string;
    }>(defaultAlertDropdownOption);
    const componentStyles = useStyles();

    useEffect(() => {
        if (selectedAlert === defaultAlertDropdownOption) {
            setAnomalies(allAnomalies);
        }
    }, [allAnomalies]);

    useEffect(() => {
        if (selectedAlert === defaultAlertDropdownOption) {
            setPreviousPeriodAnomalies(allPreviousPeriodAnomalies);
        }
    }, [allPreviousPeriodAnomalies]);

    useEffect(() => {
        getAlerts();
    }, []);

    useEffect(() => {
        if (selectedAlert !== defaultAlertDropdownOption) {
            getCurrentPeriodAnomaliesByAlert({
                alertId: selectedAlert.id,
                startTime:
                    anaylysisPeriodStartTimeMapping[selectedAnalysisPeriod]
                        .startTime,
            });
            getPreviousPeriodAnomaliesByAlert({
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
        } else {
            setAnomalies(allAnomalies);
            setPreviousPeriodAnomalies(allPreviousPeriodAnomalies);
        }
    }, [selectedAnalysisPeriod, selectedAlert]);

    useEffect(() => {
        const options =
            alerts?.map((alert) => {
                return { id: alert.id, name: alert.name };
            }) || [];
        setAlertDropdownOptions([...options, defaultAlertDropdownOption]);
    }, [alerts]);

    useEffect(() => {
        setAnomalies(currentPeriodAnomaliesByAlert);
        setPreviousPeriodAnomalies(previousPeriodAnomaliesByAlert);
    }, [currentPeriodAnomaliesByAlert, previousPeriodAnomaliesByAlert]);

    const handleAlertChange = (event): void => {
        alertDropdownOptions &&
            setSelectedAlert(
                alertDropdownOptions.find(
                    (alertDropdownOption) =>
                        alertDropdownOption.id === event.target.value
                )!
            );
    };

    return (
        <>
            <div className={componentStyles.sectionHeading}>
                <Typography variant="h6">Detection performance</Typography>
                <div className={componentStyles.alertAndRange}>
                    <AnalysisPeriod
                        analysisPeriods={analysisPeriods}
                        selectedPeriod={selectedAnalysisPeriod}
                        onClick={onAnalysisPeriodChange}
                    />
                    <Select
                        className={componentStyles.select}
                        id="alert-dropdown"
                        label="Age"
                        labelId="alert-dropdown"
                        value={selectedAlert?.id}
                        variant="outlined"
                        onChange={handleAlertChange}
                    >
                        {alertDropdownOptions?.map((alertDropdownOption) => {
                            return (
                                <MenuItem
                                    key={alertDropdownOption.id}
                                    value={alertDropdownOption.id}
                                >
                                    {alertDropdownOption.name}
                                </MenuItem>
                            );
                        })}
                    </Select>
                </div>
            </div>
            <div className={componentStyles.visualizationContainer}>
                <TotalAnomaliesGraph
                    anomalies={anomalies}
                    previousPeriodAnomalies={previousPeriodAnomalies}
                    selectedAnalysisPeriod={selectedAnalysisPeriod}
                    title="Total # of anomalies detected"
                />
                <WeekAnomaliesGraph
                    anomalies={anomalies}
                    previousPeriodAnomalies={previousPeriodAnomalies}
                    selectedAnalysisPeriod={selectedAnalysisPeriod}
                    title="Weekly anomalies detected"
                />
                <TotalAnomaliesGraph
                    anomalies={
                        anomalies?.filter((anomaly) => anomaly.notified) || null
                    }
                    notificationText="notification"
                    previousPeriodAnomalies={
                        previousPeriodAnomalies?.filter(
                            (anomaly) => anomaly.notified
                        ) || null
                    }
                    selectedAnalysisPeriod={selectedAnalysisPeriod}
                    title="Total # of notifications sent"
                />
                <WeekAnomaliesGraph
                    anomalies={
                        anomalies?.filter((anomaly) => anomaly.notified) || null
                    }
                    notificationText="notification"
                    previousPeriodAnomalies={
                        previousPeriodAnomalies?.filter(
                            (anomaly) => anomaly.notified
                        ) || null
                    }
                    selectedAnalysisPeriod={selectedAnalysisPeriod}
                    title="Weekly notifcations sent"
                />
            </div>
        </>
    );
};

export default DetectionPerformance;
