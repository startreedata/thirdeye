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
import React, { ReactElement, useRef } from "react";

// Utils
import WarningIcon from "@material-ui/icons/Warning";
import { Link as RouterLink } from "react-router-dom";
import { startCase } from "lodash";
import { getAlertsAlertPath } from "../../../utils/routes/routes.util";

// Components
import EventAvailableIcon from "@material-ui/icons/EventAvailable";
import NotificationsIcon from "@material-ui/icons/Notifications";
import TitleCard from "../../title-card/title-card.component";
import { Grid, Link, Typography } from "@material-ui/core";
import { AnalysisPeriod } from "../common/anaylysis-period/analysis-period.component";
import { CopyButton } from "../../copy-button/copy-button.component";
import { SectionHeader } from "../common/section-header/section-header.component";

// Styles
import { useSummaryStyles } from "./summary.styles";

// Data
import { useSummaryData } from "./use-summary-data";

// Interfaces
import { SummaryProps } from "./summary.interfaces";
import { useTranslation } from "react-i18next";

export const Summary = ({
    mostRecentlyInvestigatedAnomalyAlert,
    anomalies,
    previousPeriodAnomalies,
    topAlert,
    alertsCount,
    investigations,
    subscriptionGroups,
    selectedAnalysisPeriod,
    onAnalysisPeriodChange,
    analysisPeriods,
}: SummaryProps): JSX.Element => {
    const { t } = useTranslation();
    const summaryRef = useRef<HTMLDivElement>(null);
    const componentStyles = useSummaryStyles();
    const { summaryData, verboseSummaryItems } = useSummaryData({
        anomalies,
        previousPeriodAnomalies,
        selectedAnalysisPeriod,
        topAlert,
        investigations,
        alertsCount,
        subscriptionGroups,
        mostRecentlyInvestigatedAnomalyAlert,
    });

    const summaryCardsData = [
        {
            title: t("pages.impact-dashboard.sections.summary.cards.anomalies"),
            icon: <WarningIcon />,
            content: summaryData.anomalies,
        },
        {
            title: t("pages.impact-dashboard.sections.summary.cards.alerts"),
            icon: <NotificationsIcon />,
            content: summaryData.alerts,
        },
        {
            title: t(
                "pages.impact-dashboard.sections.summary.cards.notifications"
            ),
            icon: <EventAvailableIcon />,
            content: summaryData.notifications,
        },
    ];

    const renderTitleContent = (data: {
        icon: JSX.Element;
        title: string;
    }): JSX.Element => {
        return (
            <Grid container alignItems="center" direction="row" xs={12}>
                <Grid item alignItems="center" direction="row">
                    {data.icon}
                </Grid>
                <Grid item>
                    <Typography variant="h6">{data.title}</Typography>
                </Grid>
            </Grid>
        );
    };

    const renderLink = (text: string, href: string): ReactElement => {
        return (
            <Link component={RouterLink} to={href}>
                {text}
            </Link>
        );
    };

    const renderContent = (content: {
        [key: string]: { count: number; href: string };
    }): JSX.Element => {
        const contentKeys = Object.keys(content);

        return (
            <Grid container>
                <Grid container item spacing={2}>
                    {contentKeys.map((contentKey) => {
                        const count = content[contentKey].count;
                        const href = content[contentKey].href;
                        const name = startCase(contentKey);
                        const renderText = `${count} ${name}`;

                        return (
                            <Grid item key={name}>
                                {href
                                    ? renderLink(renderText, href)
                                    : renderText}
                            </Grid>
                        );
                    })}
                </Grid>
            </Grid>
        );
    };

    const getCopyContent = (): string => {
        return summaryRef.current?.textContent || "";
    };

    return (
        <>
            <div className={componentStyles.sectionHeading}>
                <SectionHeader
                    heading={t(
                        "pages.impact-dashboard.sections.summary.heading"
                    )}
                />
                <AnalysisPeriod
                    analysisPeriods={analysisPeriods}
                    selectedPeriod={selectedAnalysisPeriod}
                    onClick={onAnalysisPeriodChange}
                />
            </div>
            <Grid container spacing={3}>
                {summaryCardsData.map((data) => {
                    return (
                        <Grid item key={data.title} md={4} xs={12}>
                            <TitleCard
                                content={renderContent(data.content)}
                                title={renderTitleContent(data)}
                            />
                        </Grid>
                    );
                })}
            </Grid>
            <div className={componentStyles.verboseSummaryContainer}>
                <div className={componentStyles.verboseSummaryHeading}>
                    <div>
                        <Typography variant="h6">
                            {t(
                                "pages.impact-dashboard.sections.summary.verbose-summary-heading"
                            )}
                        </Typography>
                    </div>
                    <div>
                        <CopyButton content={getCopyContent()} />
                    </div>
                </div>
                <div ref={summaryRef}>
                    <div>
                        In the last <b>{verboseSummaryItems.weeks} weeks</b>,
                        <b>
                            {summaryData.anomalies.detected.count} anomlaies
                            were detected
                        </b>
                        , which is <b>{verboseSummaryItems.percentageChange}</b>{" "}
                        than the previous {verboseSummaryItems.weeks} weeks.{" "}
                        {summaryData.anomalies.detected.count > 0 && (
                            <>
                                Notifications about anomallies were sent via
                                Slack and Email.
                            </>
                        )}
                    </div>
                    {verboseSummaryItems.topAlert.name && (
                        <div>
                            The alert with the most anomalies is{" "}
                            <Link
                                component={RouterLink}
                                to={getAlertsAlertPath(
                                    Number(verboseSummaryItems.topAlert.id)
                                )}
                            >
                                {verboseSummaryItems.topAlert.name}.
                            </Link>
                            In the{" "}
                            <b>
                                last {verboseSummaryItems.weeks} weeks,
                                {
                                    verboseSummaryItems.topAlert.anomaliesCount
                                }{" "}
                                anomalies were detected on this metric.
                            </b>
                        </div>
                    )}
                    <div>
                        <b>
                            {verboseSummaryItems.investigation.count}{" "}
                            investigations
                        </b>{" "}
                        were performed in the last {verboseSummaryItems.weeks}{" "}
                        weeks.{" "}
                        {verboseSummaryItems.investigation.count > 0 && (
                            <span>
                                The most recent investigation was performed for
                                an anomaly that happened on{" "}
                                <b>{verboseSummaryItems.investigation.date}</b>{" "}
                                on alert{" "}
                                <Link
                                    component={RouterLink}
                                    to={getAlertsAlertPath(
                                        Number(
                                            verboseSummaryItems.investigation
                                                .alert.id
                                        )
                                    )}
                                >
                                    {
                                        verboseSummaryItems.investigation.alert
                                            .name
                                    }
                                    .
                                </Link>
                            </span>
                        )}
                    </div>
                </div>
            </div>
        </>
    );
};
