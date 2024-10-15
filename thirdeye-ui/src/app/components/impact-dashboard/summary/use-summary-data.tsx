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
import {
    Summary,
    SummaryData,
    SummaryDataProps,
    VerboseSummary,
} from "./summary.interfaces";

// Utils
import {
    getAlertsAllPath,
    getAnomaliesAllPath,
    getSubscriptionGroupsAllPath,
} from "../../../utils/routes/routes.util";
import { epochToDate } from "../detection-performance/util";
import { isEmpty, uniq } from "lodash";

export const useSummaryData = ({
    alerts,
    mostRecentlyInvestigatedAnomalyAlert,
    anomalies,
    previousPeriodAnomalies,
    topAlert,
    investigations,
    subscriptionGroups,
    selectedAnalysisPeriod,
    activeEnumerationItems,
}: SummaryDataProps): Summary => {
    const selectedAnalysisPeriodDisplayText = selectedAnalysisPeriod.substring(
        0,
        selectedAnalysisPeriod.length - 1
    );
    const [summaryData, setSummaryData] = useState<SummaryData>({
        anomalies: {
            detected: { count: 0, href: getAnomaliesAllPath() },
            investigations: { count: 0, href: "" },
        },
        alerts: {
            activeAlerts: { count: 0, href: getAlertsAllPath() },
            activeDimensions: { count: 0, href: "" },
        },
        notifications: {
            notificationsSent: { count: 0, href: "" },
            groups: { count: 0, href: getSubscriptionGroupsAllPath() },
        },
    });

    const [verboseSummaryItems, setVerboseSummaryItems] =
        useState<VerboseSummary>({
            weeks: selectedAnalysisPeriodDisplayText,
            percentageChange: "0% more",
            topAlert: {
                id: null,
                name: "",
                anomaliesCount: 0,
            },
            investigation: {
                count: 0,
                date: "",
                alert: {
                    id: null,
                    name: "",
                },
            },
            notificationChannelsUsed: [],
        });

    useEffect(() => {
        const allAlertIds = anomalies?.map((anomaly) => anomaly.alert.id);
        const allChannels: VerboseSummary["notificationChannelsUsed"] = [];
        subscriptionGroups?.forEach((subscriptionGroup) => {
            const assoaciatedAlerts = subscriptionGroup.alertAssociations?.map(
                (alertAssociation) => alertAssociation.alert.id
            );
            if (
                assoaciatedAlerts?.some((assoaciatedAlert) =>
                    allAlertIds?.includes(assoaciatedAlert)
                )
            ) {
                allChannels.push(
                    ...subscriptionGroup.specs.map((spec) => spec.type)
                );
            }
        });
        if (!isEmpty(allChannels)) {
            setVerboseSummaryItems((prevState) => {
                return {
                    ...prevState,
                    notificationChannelsUsed: uniq(allChannels),
                };
            });
        }
    }, [anomalies, alerts, subscriptionGroups]);

    useEffect(() => {
        if (activeEnumerationItems) {
            setSummaryData((prevState) => {
                return {
                    ...prevState,
                    alerts: {
                        activeAlerts: prevState.alerts.activeAlerts,
                        activeDimensions: {
                            count: activeEnumerationItems.length,
                            href: "",
                        },
                    },
                };
            });
        }
    }, [activeEnumerationItems]);

    useEffect(() => {
        if (isEmpty(anomalies) && isEmpty(previousPeriodAnomalies)) {
            setVerboseSummaryItems((prevState) => {
                return {
                    ...prevState,
                    weeks: selectedAnalysisPeriodDisplayText,
                    percentageChange: "0% more",
                    topAlert: {
                        id: null,
                        name: "",
                        anomaliesCount: 0,
                    },
                };
            });
        }
        if (isEmpty(anomalies) && !isEmpty(previousPeriodAnomalies)) {
            setVerboseSummaryItems((prevState) => {
                return {
                    ...prevState,
                    weeks: selectedAnalysisPeriodDisplayText,
                    percentageChange: "100% less",
                    topAlert: {
                        id: null,
                        name: "",
                        anomaliesCount: 0,
                    },
                };
            });
        }
        if (!isEmpty(anomalies) && isEmpty(previousPeriodAnomalies)) {
            setVerboseSummaryItems((prevState) => {
                return {
                    ...prevState,
                    weeks: selectedAnalysisPeriodDisplayText,
                    percentageChange: "100% more",
                };
            });
        }
        if (!isEmpty(anomalies) && !isEmpty(previousPeriodAnomalies)) {
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
                return {
                    ...prevState,
                    percentageChange: percentChange,
                };
            });
        }
    }, [anomalies, previousPeriodAnomalies]);

    useEffect(() => {
        if (!isEmpty(topAlert)) {
            setVerboseSummaryItems((prevState) => {
                return {
                    ...prevState,
                    topAlert: {
                        id: Number(topAlert.id),
                        name: topAlert.name,
                        anomaliesCount: topAlert.anomaliesCount,
                    },
                };
            });
        }
    }, [topAlert]);

    useEffect(() => {
        let notificationsSent = 0;
        anomalies?.forEach((anomaly) => {
            if (anomaly.notified) {
                notificationsSent++;
            }
        });
        setSummaryData((prevState) => {
            return {
                ...prevState,
                anomalies: {
                    detected: {
                        count: anomalies?.length || 0,
                        href: prevState.anomalies.detected.href,
                    },
                    investigations: prevState.anomalies.investigations,
                },
                notifications: {
                    notificationsSent: {
                        count: notificationsSent,
                        href: prevState.notifications.notificationsSent.href,
                    },
                    groups: prevState.notifications.groups,
                },
            };
        });
    }, [anomalies]);

    useEffect(() => {
        setSummaryData((prevState) => {
            return {
                ...prevState,
                alerts: {
                    activeAlerts: {
                        count: alerts?.length || 0,
                        href: prevState.alerts.activeAlerts.href,
                    },
                    activeDimensions: { count: 0, href: "" },
                },
            };
        });
    }, [alerts?.length]);

    useEffect(() => {
        setSummaryData((prevState) => {
            return {
                ...prevState,
                notifications: {
                    notificationsSent:
                        prevState.notifications.notificationsSent,
                    groups: {
                        count: subscriptionGroups?.length || 0,
                        href: prevState.notifications.groups.href,
                    },
                },
            };
        });
    }, [subscriptionGroups]);

    useEffect(() => {
        setSummaryData((prevState) => {
            return {
                ...prevState,
                anomalies: {
                    detected: prevState.anomalies.detected,
                    investigations: {
                        count: investigations?.length || 0,
                        href: prevState.anomalies.investigations.href,
                    },
                },
            };
        });

        setVerboseSummaryItems((prevState) => {
            let recentInvestigation;
            if (investigations && investigations.length) {
                recentInvestigation = investigations[investigations.length - 1];
            }

            return {
                ...prevState,
                investigation: {
                    count: investigations?.length || 0,
                    date: recentInvestigation
                        ? epochToDate(recentInvestigation.created)
                        : "",
                    alert: { ...prevState.investigation.alert },
                },
            };
        });
    }, [investigations]);

    useEffect(() => {
        setVerboseSummaryItems((prevState) => {
            return {
                ...prevState,
                investigation: {
                    count: prevState.investigation.count,
                    date: prevState.investigation.date,
                    alert: {
                        id: mostRecentlyInvestigatedAnomalyAlert?.id,
                        name: alerts?.find(
                            (alert) =>
                                alert.id ===
                                mostRecentlyInvestigatedAnomalyAlert?.id
                        )?.name,
                    },
                },
            };
        });
    }, [mostRecentlyInvestigatedAnomalyAlert]);

    useEffect(() => {
        setVerboseSummaryItems((prevState) => {
            return {
                ...prevState,
                weeks: selectedAnalysisPeriodDisplayText,
            };
        });
    }, [selectedAnalysisPeriodDisplayText]);

    return { summaryData, verboseSummaryItems };
};
