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
import { useGetAlerts } from "../../rest/alerts/alerts.actions";
import { getDatasetByName } from "../../rest/datasets/datasets.rest";
import { Dataset } from "../../rest/dto/dataset.interfaces";

export const ImpactDashboardPage: FunctionComponent = () => {
    const { t } = useTranslation();
    const today = new Date();
    const date28DaysAgo = new Date(today);
    date28DaysAgo.setDate(today.getDate() - 56);
    const defaultStartDate = date28DaysAgo.getTime();

    const { anomalyStats, getAnomalyStats } = useGetAnomalyStats();
    const { alerts, getAlerts, status, errorMessages, resetData } =
        useGetAlerts();
    const { anomalies, getAnomalies, status } = useGetAnomalies();
    const [startTime, setStartTime] = useState<number>(defaultStartDate);

    const [mostActiveAlerts, setMostActiveAlerts] = useState<[string, any][]>();
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
    const [verboseSummaryItems, setVerboseSummaryItems] = useState({
        weeks: 4,
        percentageChange: 0,
        topAlert: {
            id: "",
            name: "",
            anomaliesCount: 0,
        },
        investigation: {
            count: 0,
            date: "",
            alert: {
                id: "",
                name: "",
            },
        },
    });

    useEffect(() => {
        getAnomalyStats({ startTime: startTime }); // on passing end time data is coming as blank
        getAnomalies({ startTime: startTime });
        getAlerts();
    }, []);

    useEffect(() => {
        // TODO: add interface for anomaly type
        if (anomalies) {
            const alertData: { [key: number]: [any] } = {};
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
            setMostActiveAlerts(topFiveActiveAlerts);
            setVerboseSummaryItems({
                ...verboseSummaryItems,
                topAlert: {
                    id: topFiveActiveAlerts[0][0],
                    name: topFiveActiveAlerts[0][1][0].alert.name,
                    anomaliesCount: topFiveActiveAlerts[0][1].length,
                },
            });
        }
    }, [anomalies]);

    useEffect(() => {
        if (mostActiveAlerts) {
            const dimensionPromises: Promise<Dataset>[] = [];
            mostActiveAlerts.forEach((alert) => {
                dimensionPromises.push(
                    getDatasetByName(alert[1][0].metadata.dataset.name)
                );
            });
            Promise.all(dimensionPromises).then((data) => {
                const alertDimensionMapping: { [key: string]: string[] }[] = [];
                data.forEach((d, idx) => {
                    alertDimensionMapping.push({
                        [mostActiveAlerts[idx][0]]: d.dimensions,
                    });
                });
                // setActiveAlertsDimensions(alertDimensionMapping)
            });
        }
    }, [mostActiveAlerts]);

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
                investigations: 0,
            },
            alerts: {
                activeAlerts: alerts?.length || 0,
                activeDimensions: 0,
            },
            notifications: {
                notificationsSent: notificationsSent,
                groups: summaryData.notifications.groups,
            },
        });
    }, [alerts, anomalyStats, anomalies]);

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
                subHeading="Description goes here"
            />
            <Summary
                summaryData={summaryData}
                verboseSummaryItems={verboseSummaryItems}
            />
        </MainLayout>
    );
};
