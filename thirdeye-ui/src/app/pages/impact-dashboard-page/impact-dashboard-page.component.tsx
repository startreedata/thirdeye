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
import React, { FunctionComponent, useEffect, useState } from "react";

import { MainLayout } from "../../platform/components/layout/main/page.component";
import { HelpDrawerV1 } from "../../components/help-drawer-v1/help-drawer-v1.component";
import { alertsBasicHelpCards } from "../../components/help-drawer-v1/help-drawer-card-contents.utils";
import { Box, Button } from "@material-ui/core";
import { Icon } from "@iconify/react";
import { useTranslation } from "react-i18next";
import PageHeader from "../../platform/components/layout/page-header/page-header.component";
import Summary from "../../components/impact-dashboard/summary/summary.component";
import {
    useGetAnomalies,
    useGetAnomalyStats,
} from "../../rest/anomalies/anomaly.actions";
import {
    useGetAlertStats,
    useGetAlerts,
    useGetAlertsCount,
} from "../../rest/alerts/alerts.actions";
import { getDatasetByName } from "../../rest/datasets/datasets.rest";
import { Dataset } from "../../rest/dto/dataset.interfaces";
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import {
    AlertEffectivnessData,
    VerboseSummary,
} from "./impact-dashboard-page.interfaces";
import AlertEffectiveness from "../../components/impact-dashboard/alert-effectiveness/alert-effectiveness.component";
import { isEmpty } from "lodash";
import DetectionPerformance from "../../components/impact-dashboard/detection-performance/detection-performance.component";
import {
    anaylysisPeriodStartTimeMapping,
    anaylysisPeriodPreviousWindowTimeMapping,
} from "../../platform/utils";
import RecentInvestigations from "../../components/impact-dashboard/investigations/investigations.component";
import { useGetInvestigations } from "../../rest/rca/rca.actions";
import { epochToDate } from "../../components/impact-dashboard/detection-performance/util";
import { Alert } from "../../rest/dto/alert.interfaces";
const anaylysisPeriods = ["4w", "13w", "26w"];

export const ImpactDashboardPage: FunctionComponent = () => {
    const { t } = useTranslation();
    const [selectedAnalysisPeriod, setSelectedAnalysisPeriod] =
        useState<string>("4w");
    const { anomalyStats, getAnomalyStats } = useGetAnomalyStats();

    const {
        alertsConfigured,
        getAlertsCount,
        status,
        errorMessages,
        resetData,
    } = useGetAlertsCount();
    const { anomalies, getAnomalies, status } = useGetAnomalies();
    const {
        anomalies: previousPeriodAnomalies,
        getAnomalies: getPreviousPeriodAnomalies,
    } = useGetAnomalies();
    const { investigations, getInvestigations } = useGetInvestigations();

    const [anomalyGroupByAlertId, setAnomalyGroupByAlertId] =
        useState<[string, Anomaly[]][]>();
    const [activeAlertsDimensions, setActiveAlertsDimensions] =
        useState<{ [key: string]: string[] }>();

    const [summaryData, setSummaryData] = useState({
        anomalies: {
            detected: 0,
            investigations: 0,
        },
        alerts: {
            activeAlerts: 0,
            activeDimensions: 0,
        },
        notifications: {
            notificationsSent: 0,
            groups: 0,
        },
    });
    const [verboseSummaryItems, setVerboseSummaryItems] =
        useState<VerboseSummary>({
            weeks: selectedAnalysisPeriod,
            percentageChange: "0% more",
            topAlert: {
                id: null,
                name: "",
                anomaliesCount: 0,
            },
            investigation: {
                count: 0,
                date: "",
                anomaly: {
                    id: null,
                    name: "",
                },
            },
        });

    useEffect(() => {
        if (selectedAnalysisPeriod) {
            const currentPeriod =
                anaylysisPeriodStartTimeMapping[selectedAnalysisPeriod];
            const previousPeriod =
                anaylysisPeriodPreviousWindowTimeMapping[
                    selectedAnalysisPeriod
                ];
            getAnomalyStats({ startTime: currentPeriod.startTime }); // on passing end time data is coming as blank
            getAnomalies({ startTime: currentPeriod.startTime });
            getPreviousPeriodAnomalies({
                startTime: previousPeriod.startTime,
                endTime: previousPeriod.endTime,
            });
            getInvestigations(undefined, currentPeriod.startTime);
            getAlertsCount();
            setVerboseSummaryItems({
                ...verboseSummaryItems,
                weeks: selectedAnalysisPeriod,
            });
        }
    }, [selectedAnalysisPeriod]);

    useEffect(() => {
        if (!isEmpty(anomalies)) {
            const alertData: { [key: number]: Anomaly[] } = {};
            anomalies &&
                anomalies.forEach((anomaly) => {
                    if (!alertData[anomaly.alert.id]) {
                        alertData[anomaly.alert.id] = [anomaly];
                    } else {
                        alertData[anomaly.alert.id].push(anomaly);
                    }
                });
            const groupedAnomalies = Object.entries(alertData);
            groupedAnomalies.sort((a, b) => b[1].length - a[1].length);
            const topFiveActiveAlerts = groupedAnomalies.slice(0, 5);
            setAnomalyGroupByAlertId(topFiveActiveAlerts);
            let percentChange = "0 % more";
            if (anomalies && previousPeriodAnomalies) {
                const currentPeriodAnomaliesCount = anomalies.length;
                const prevoiusPeriodAnomaliesCount =
                    previousPeriodAnomalies.length;
                if (
                    currentPeriodAnomaliesCount > prevoiusPeriodAnomaliesCount
                ) {
                    percentChange = `${(
                        ((currentPeriodAnomaliesCount -
                            prevoiusPeriodAnomaliesCount) *
                            100) /
                        prevoiusPeriodAnomaliesCount
                    ).toFixed(2)}% more`;
                } else {
                    percentChange = `${(
                        ((prevoiusPeriodAnomaliesCount -
                            currentPeriodAnomaliesCount) *
                            100) /
                        prevoiusPeriodAnomaliesCount
                    ).toFixed(2)}% less`;
                }
            }
            setVerboseSummaryItems((prevState) => {
                let recentInvestigation;
                if (investigations && investigations.length) {
                    recentInvestigation =
                        investigations[investigations.length - 1];
                }

                return {
                    ...prevState,
                    percentageChange: percentChange,
                    investigation: {
                        count: investigations?.length || 0,
                        date: recentInvestigation
                            ? epochToDate(recentInvestigation.created)
                            : "",
                        anomaly: {
                            id: recentInvestigation
                                ? recentInvestigation.anomaly?.id
                                : null,
                            name: "",
                        },
                    },
                    topAlert: {
                        id: Number(topFiveActiveAlerts[0][0]),
                        name: topFiveActiveAlerts[0][1][0].alert.name,
                        anomaliesCount: topFiveActiveAlerts[0][1].length,
                    },
                };
            });
        } else {
            setAnomalyGroupByAlertId([]);
            setVerboseSummaryItems({
                weeks: selectedAnalysisPeriod,
                percentageChange: "0% more",
                topAlert: {
                    id: null,
                    name: "",
                    anomaliesCount: 0,
                },
                investigation: {
                    count: 0,
                    date: "",
                    anomaly: {
                        id: null,
                        name: "",
                    },
                },
            });
        }
    }, [anomalies, previousPeriodAnomalies, investigations]);

    useEffect(() => {
        if (!isEmpty(anomalyGroupByAlertId)) {
            const dimensionPromises: Promise<Dataset>[] = [];
            anomalyGroupByAlertId!.forEach((group) => {
                group[1][1];
                dimensionPromises.push(
                    getDatasetByName(group[1][0].metadata.dataset!.name)
                );
            });
            Promise.all(dimensionPromises).then((data) => {
                const alertDimensionMapping: { [key: string]: string[] } = {};
                data.forEach((d, idx) => {
                    alertDimensionMapping[anomalyGroupByAlertId![idx][0]] =
                        d.dimensions;
                });
                setActiveAlertsDimensions(alertDimensionMapping);
            });
        }
    }, [anomalyGroupByAlertId]);

    const alertEffectivenessData = (): AlertEffectivnessData[] => {
        const data: AlertEffectivnessData[] = [];
        anomalyGroupByAlertId?.forEach((group) => {
            data.push({
                id: group[0],
                name: group[1][0].alert.name,
                anomaliesCount: group[1].length,
                dimensionsCount: activeAlertsDimensions
                    ? activeAlertsDimensions[group[0]]?.length
                    : 0,
            });
        });

        return data;
    };
    useEffect(() => {
        let notificationsSent = 0;
        anomalies?.forEach((anomaly) => {
            if (anomaly.notified) {
                notificationsSent++;
            }
        });
        setSummaryData({
            ...summaryData,
            anomalies: {
                detected: anomalyStats?.totalCount || 0,
                investigations: investigations?.length || 0,
            },
            alerts: {
                activeAlerts: alertsConfigured?.count || 0,
                activeDimensions: 0,
            },
            notifications: {
                notificationsSent: notificationsSent,
                groups: summaryData.notifications.groups,
            },
        });
    }, [alertsConfigured, anomalyStats, anomalies, investigations]);

    const renderActionButtons = (): JSX.Element => {
        return (
            <HelpDrawerV1
                cards={alertsBasicHelpCards}
                title={`${t("label.need-help")}?`}
                trigger={(handleOpen) => (
                    <Button
                        color="primary"
                        size="small"
                        variant="outlined"
                        onClick={handleOpen}
                    >
                        <Box component="span" mr={1}>
                            {t("label.need-help")}
                        </Box>
                        <Box component="span" display="flex">
                            <Icon
                                fontSize={24}
                                icon="mdi:question-mark-circle-outline"
                            />
                        </Box>
                    </Button>
                )}
            />
        );
    };

    return (
        <MainLayout>
            <PageHeader
                actionButtons={renderActionButtons()}
                mainHeading={t("label.impact-dashboard")}
                subHeading="Overview of system statistics, anomaly detection performance, and usage insights."
            />
            <Summary
                analysisPeriods={anaylysisPeriods}
                selectedAnalysisPeriod={selectedAnalysisPeriod}
                summaryData={summaryData}
                verboseSummaryItems={verboseSummaryItems}
                onAnalysisPeriodChange={(period) =>
                    setSelectedAnalysisPeriod(period)
                }
            />
            <DetectionPerformance
                analysisPeriods={anaylysisPeriods}
                anomalies={anomalies}
                previousPeriodAnomalies={previousPeriodAnomalies}
                selectedAnalysisPeriod={selectedAnalysisPeriod}
                onAnalysisPeriodChange={(period) =>
                    setSelectedAnalysisPeriod(period)
                }
            />
            <AlertEffectiveness
                analysisPeriods={anaylysisPeriods}
                mostActiveAlerts={alertEffectivenessData()}
                selectedAnalysisPeriod={selectedAnalysisPeriod}
                onAnalysisPeriodChange={(period) =>
                    setSelectedAnalysisPeriod(period)
                }
            />
            <RecentInvestigations
                analysisPeriods={anaylysisPeriods}
                anomalies={anomalies}
                investigations={investigations}
                selectedAnalysisPeriod={selectedAnalysisPeriod}
                onAnalysisPeriodChange={(period) =>
                    setSelectedAnalysisPeriod(period)
                }
            />
        </MainLayout>
    );
};
