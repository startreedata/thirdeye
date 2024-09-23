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
import { useTranslation } from "react-i18next";

// Styles
import { useDetectionPerformanceStyles } from "./detection-performance.styles";

// Components
import { AnalysisPeriod } from "../common/anaylysis-period/analysis-period.component";
import { LineGraph } from "./line-graph.component";
import { WeeklyGraph } from "./weekly-graph.component";
import { SectionHeader } from "../common/section-header/section-header.component";
import { MenuItem, Select } from "@material-ui/core";

// Interfaces
import { DetectionPerformanceProps } from "./detection-performance.interfaces";

// APIs
import { useDetectionPerformanceApiRequests } from "./api";

// Data
import { useGetDetectionPerformanceData } from "./data";

const defaultAlertDropdownOption = { id: -1, name: "All alerts" };

export const DetectionPerformance = ({
    anomalies: allAnomalies,
    previousPeriodAnomalies: allPreviousPeriodAnomalies,
    analysisPeriods,
    selectedAnalysisPeriod,
    onAnalysisPeriodChange,
}: DetectionPerformanceProps): ReactElement => {
    const { t } = useTranslation();
    const [alertDropdownOptions, setAlertDropdownOptions] =
        useState<{ id: number; name: string }[]>();
    const [selectedAlert, setSelectedAlert] = useState<{
        id: number;
        name: string;
    }>(defaultAlertDropdownOption);

    const {
        alerts,
        currentPeriodAnomaliesByAlert,
        previousPeriodAnomaliesByAlert,
    } = useDetectionPerformanceApiRequests({
        selectedAlert,
        defaultAlertDropdownOption,
        selectedAnalysisPeriod,
    });
    const { anomalies, previousPeriodAnomalies } =
        useGetDetectionPerformanceData({
            selectedAlert,
            defaultAlertDropdownOption,
            allAnomalies,
            allPreviousPeriodAnomalies,
            currentPeriodAnomaliesByAlert,
            previousPeriodAnomaliesByAlert,
        });

    const componentStyles = useDetectionPerformanceStyles();

    useEffect(() => {
        const options =
            alerts?.map((alert) => {
                return { id: alert.id, name: alert.name };
            }) || [];
        setAlertDropdownOptions([...options, defaultAlertDropdownOption]);
    }, [alerts]);

    const handleAlertChange = (
        event: React.ChangeEvent<{ value: unknown }>
    ): void => {
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
                <SectionHeader
                    heading={t(
                        "pages.impact-dashboard.sections.detection-performance.heading"
                    )}
                />
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
                <LineGraph
                    anomalies={anomalies}
                    previousPeriodAnomalies={previousPeriodAnomalies}
                    selectedAnalysisPeriod={selectedAnalysisPeriod}
                    title={t(
                        "pages.impact-dashboard.sections.detection-performance.total-anomalies-graph-title"
                    )}
                />
                <WeeklyGraph
                    anomalies={anomalies}
                    previousPeriodAnomalies={previousPeriodAnomalies}
                    selectedAnalysisPeriod={selectedAnalysisPeriod}
                    title={t(
                        "pages.impact-dashboard.sections.detection-performance.total-anomalies-weekly-graph-title"
                    )}
                />
                <LineGraph
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
                    title={t(
                        "pages.impact-dashboard.sections.detection-performance.total-notifications-graph-title"
                    )}
                />
                <WeeklyGraph
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
                    title={t(
                        "pages.impact-dashboard.sections.detection-performance.total-notifications-weekly-graph-title"
                    )}
                />
            </div>
        </>
    );
};
