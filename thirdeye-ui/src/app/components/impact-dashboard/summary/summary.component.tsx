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
import React, { useRef } from "react";
import TitleCard from "../../title-card/title-card.component";
import NotificationsIcon from "@material-ui/icons/Notifications";
import EventAvailableIcon from "@material-ui/icons/EventAvailable";
import WarningIcon from "@material-ui/icons/Warning";
import { Grid, Link, Typography } from "@material-ui/core";
import { SummaryProps } from "./summary.interfaces";
import { startCase } from "lodash";
import { Link as RouterLink } from "react-router-dom";
import { getAlertsAlertPath } from "../../../utils/routes/routes.util";
import AnalysisPeriod from "../anaylysis-period/analysis-period.component";
import { CopyButton } from "../../copy-button/copy-button.component";
import { useStyles } from "./summary.styles";

const Summary = ({
    summaryData,
    verboseSummaryItems,
    selectedAnalysisPeriod,
    onAnalysisPeriodChange,
    analysisPeriods,
}: SummaryProps): JSX.Element => {
    const summaryRef = useRef<HTMLDivElement>(null);
    const componentStyles = useStyles();
    const summaryCardsData = [
        {
            title: "Anomalies",
            icon: <WarningIcon />,
            content: summaryData.anomalies,
        },
        {
            title: "Alerts",
            icon: <NotificationsIcon />,
            content: summaryData.alerts,
        },
        {
            title: "Notifications",
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

    const renderContent = (content): JSX.Element => {
        const keys = Object.keys(content);

        return (
            <Grid container>
                <Grid container item spacing={2}>
                    <Grid item>
                        {`${content[keys[0]]} ${startCase(keys[0])}`}
                    </Grid>
                    <Grid item>
                        {`${content[keys[1]]} ${startCase(keys[1])}`}
                    </Grid>
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
                <Typography variant="h6">System stats</Typography>
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
                        <Typography variant="h6">Summary</Typography>
                    </div>
                    <div>
                        <CopyButton content={getCopyContent()} />
                    </div>
                </div>
                <div ref={summaryRef}>
                    <div>
                        In the last <b>{verboseSummaryItems.weeks} weeks</b>,
                        <b>
                            {summaryData.anomalies.detected} anomlaies were
                            detected
                        </b>
                        , which is <b>{verboseSummaryItems.percentageChange}</b>{" "}
                        than the previous {verboseSummaryItems.weeks} weeks.{" "}
                        {summaryData.anomalies.detected > 0 && (
                            <>
                                Notifications about anomallies were sent via
                                Slack and Email.
                            </>
                        )}
                    </div>
                    {verboseSummaryItems.topAlert.name && (
                        <div>
                            The metric with the most anomalies is{" "}
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
                                on metric{" "}
                                {verboseSummaryItems.investigation.anomaly.name}
                            </span>
                        )}
                    </div>
                </div>
            </div>
        </>
    );
};

export default Summary;
