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
import React, { ReactElement, useMemo } from "react";
import { useTranslation } from "react-i18next";

// Interfaces
import { AlertEffectivenessProps } from "./usage-insights.interfaces";
import {
    TableColumns,
    TableRow,
} from "../../../platform/components/table/table.interfaces";

// Components
import DataTable from "../../../platform/components/table/table.component";
import { Link } from "@material-ui/core";
import { Link as RouterLink } from "react-router-dom";
import { AnalysisPeriod } from "../common/anaylysis-period/analysis-period.component";
import { SectionHeader } from "../common/section-header/section-header.component";

// Styles
import { usageInsightsStyle } from "./usage-insights.styles";

// Utils
import {
    getAlertsAlertPath,
    getSubscriptionGroupsViewPath,
} from "../../../utils/routes/routes.util";

export const UsageInsights = ({
    anomalies,
    subscriptionGroups,
    mostActiveAlerts,
    analysisPeriods,
    selectedAnalysisPeriod,
    onAnalysisPeriodChange,
}: AlertEffectivenessProps): ReactElement => {
    const { t } = useTranslation();
    const componentStyles = usageInsightsStyle();

    /* Top notifications group are calculated by count of anomalies for alerts registered for
    that subscription group */
    const topNotificationGroupData = useMemo(() => {
        const data: { id: number; name: string; anomaliesCount: number }[] = [];
        subscriptionGroups?.forEach((group) => {
            let anomaliesCount = 0;
            const alertIds = group.alertAssociations?.map(
                (alertAssociation) => alertAssociation.alert.id
            );
            alertIds?.forEach((alertId) => {
                anomaliesCount =
                    anomaliesCount +
                    (mostActiveAlerts?.find(
                        (mostActiveAlert) =>
                            mostActiveAlert.id === alertId.toString()
                    )?.anomaliesCount || 0);
            });
            anomaliesCount &&
                data.push({
                    id: group.id,
                    name: group.name,
                    anomaliesCount: anomaliesCount,
                });
        });

        return data;
    }, [anomalies, subscriptionGroups, mostActiveAlerts]);

    const renderAlertLink = (data: TableRow): ReactElement => {
        const id = Number(data.id);

        return (
            <Link component={RouterLink} to={getAlertsAlertPath(id)}>
                {data.name}
            </Link>
        );
    };
    const renderSubscriptionLink = (data: TableRow): ReactElement => {
        const id = Number(data.id);

        return (
            <Link component={RouterLink} to={getSubscriptionGroupsViewPath(id)}>
                {data.name}
            </Link>
        );
    };
    const topAnomalycolumns: TableColumns[] = [
        {
            title: t(
                "pages.impact-dashboard.sections.usage-insights.anomaly-table.columns.alert-name"
            ),
            datakey: "name",
            customRender: renderAlertLink,
        },
        {
            title: t(
                "pages.impact-dashboard.sections.usage-insights.anomaly-table.columns.anomalies-detected"
            ),
            datakey: "anomaliesCount",
        },
        {
            title: t(
                "pages.impact-dashboard.sections.usage-insights.anomaly-table.columns.dimensions-within-alert"
            ),
            datakey: "dimensionsCount",
        },
    ];

    const topAnomalyNotifcationGroupscolumns: TableColumns[] = [
        {
            title: t(
                "pages.impact-dashboard.sections.usage-insights.notification-group-table.columns.subscription-group"
            ),
            datakey: "name",
            customRender: renderSubscriptionLink,
        },
        {
            title: t(
                "pages.impact-dashboard.sections.usage-insights.notification-group-table.columns.anomalies"
            ),
            datakey: "anomaliesCount",
        },
    ];

    return (
        <>
            <div className={componentStyles.sectionHeading}>
                <SectionHeader
                    heading={t(
                        "pages.impact-dashboard.sections.usage-insights.heading"
                    )}
                />
                <AnalysisPeriod
                    analysisPeriods={analysisPeriods}
                    selectedPeriod={selectedAnalysisPeriod}
                    onClick={onAnalysisPeriodChange}
                />
            </div>
            <div className={componentStyles.tablesContainer}>
                <div>
                    <div className={componentStyles.tableHeading}>
                        {t(
                            "pages.impact-dashboard.sections.usage-insights.anomaly-table.title"
                        )}
                    </div>
                    <div className={componentStyles.table}>
                        <DataTable
                            columns={topAnomalycolumns}
                            data={mostActiveAlerts}
                            emptyStateView={t(
                                "pages.impact-dashboard.sections.usage-insights.anomaly-table.emptyState"
                            )}
                        />
                    </div>
                </div>
                <div>
                    <div className={componentStyles.tableHeading}>
                        {t(
                            "pages.impact-dashboard.sections.usage-insights.notification-group-table.title"
                        )}
                    </div>
                    <div className={componentStyles.table}>
                        <DataTable
                            columns={topAnomalyNotifcationGroupscolumns}
                            data={topNotificationGroupData}
                            emptyStateView={t(
                                "pages.impact-dashboard.sections.usage-insights.notification-group-table.emptyState"
                            )}
                        />
                    </div>
                </div>
            </div>
        </>
    );
};
