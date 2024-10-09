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

// Components
import { PageHeader } from "../../platform/components/layout/page-header/page-header.component";
import { MainLayout } from "../../platform/components/layout/main/page.component";
import { HelpDrawerV1 } from "../../components/help-drawer-v1/help-drawer-v1.component";
import {
    DetectionPerformance,
    RecentInvestigations,
    Summary,
    UsageInsights,
} from "../../components/impact-dashboard";
import { Box, Button } from "@material-ui/core";
import { Icon } from "@iconify/react";

// Utils
import { alertsBasicHelpCards } from "../../components/help-drawer-v1/help-drawer-card-contents.utils";
import { useTranslation } from "react-i18next";
import { isEmpty } from "lodash";

// APIs
import { useImpactDashBoardApiRequests } from "./data/use-impact-dashboard-api-requests";

// Interfaces
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import { AlertEffectivnessData } from "./impact-dashboard-page.interfaces";
import { EnumerationItem } from "../../rest/dto/enumeration-item.interfaces";

const anaylysisPeriods = ["4w", "13w", "26w"];

export const ImpactDashboardPage: FunctionComponent = () => {
    const { t } = useTranslation();
    const [selectedAnalysisPeriod, setSelectedAnalysisPeriod] =
        useState<string>("4w");

    const [anomalyGroupByAlertId, setAnomalyGroupByAlertId] =
        useState<[string, Anomaly[]][]>();
    const [activeAlertsDimensions, setActiveAlertsDimensions] =
        useState<{ [key: string]: EnumerationItem[] }>();

    /* Making all the API requests that we need for the entire page and results of which are being
    used in different sections */
    const {
        anomalies,
        previousPeriodAnomalies,
        investigations,
        alerts,
        subscriptionGroups,
        mostRecentlyInvestigatedAnomalyAlert,
        enumerationItems,
    } = useImpactDashBoardApiRequests({ selectedAnalysisPeriod });

    useEffect(() => {
        /* From anomalies, we group the alerts with anomalies and order them in descending order
        of anomaly count for alerts */
        if (!isEmpty(anomalies)) {
            const alertData: { [key: number]: Anomaly[] } = {};
            anomalies &&
                anomalies.forEach((anomaly: Anomaly) => {
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
        } else {
            setAnomalyGroupByAlertId([]);
        }
    }, [anomalies, previousPeriodAnomalies]);

    useEffect(() => {
        if (!isEmpty(anomalyGroupByAlertId)) {
            const alertEnumerationItemMapping: {
                [key: number]: EnumerationItem[];
            } = {};
            enumerationItems?.forEach((enumerationItem) => {
                const alertId = enumerationItem.alert?.id;
                if (alertId) {
                    alertEnumerationItemMapping[alertId] =
                        alertEnumerationItemMapping[alertId]
                            ? [
                                  enumerationItem,
                                  ...alertEnumerationItemMapping[alertId],
                              ]
                            : [enumerationItem];
                }
            });
            setActiveAlertsDimensions(alertEnumerationItemMapping);
        }
    }, [anomalyGroupByAlertId]);

    // Creates an array of alerts with anomlaies and dimensions count.
    const alertEffectivenessData = (): AlertEffectivnessData[] => {
        const data: AlertEffectivnessData[] = [];
        anomalyGroupByAlertId?.forEach((group) => {
            data.push({
                id: group[0],
                name:
                    alerts?.find((alert) => alert.id === Number(group[0]))
                        ?.name || "",
                anomaliesCount: group[1].length,
                dimensionsCount: activeAlertsDimensions
                    ? activeAlertsDimensions[group[0]]?.length || 1
                    : 1,
            });
        });

        return data;
    };

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
                mainHeading={t("pages.impact-dashboard.title")}
                subHeading={t("pages.impact-dashboard.subHeading")}
            />
            <Summary
                activeEnumerationItems={enumerationItems}
                alerts={alerts}
                analysisPeriods={anaylysisPeriods}
                anomalies={anomalies}
                investigations={investigations}
                mostRecentlyInvestigatedAnomalyAlert={
                    mostRecentlyInvestigatedAnomalyAlert
                }
                previousPeriodAnomalies={previousPeriodAnomalies}
                selectedAnalysisPeriod={selectedAnalysisPeriod}
                subscriptionGroups={subscriptionGroups}
                topAlert={alertEffectivenessData()[0]}
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
            <UsageInsights
                analysisPeriods={anaylysisPeriods}
                anomalies={anomalies}
                mostActiveAlerts={alertEffectivenessData()}
                selectedAnalysisPeriod={selectedAnalysisPeriod}
                subscriptionGroups={subscriptionGroups}
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
