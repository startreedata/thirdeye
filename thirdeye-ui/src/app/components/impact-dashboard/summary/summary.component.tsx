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
import React from "react";
import TitleCard from "../../title-card/title-card.component";
import NotificationsIcon from "@material-ui/icons/Notifications";
import EventAvailableIcon from "@material-ui/icons/EventAvailable";
import WarningIcon from "@material-ui/icons/Warning";
import { Box, Grid, Link, Typography } from "@material-ui/core";
import { SummaryProps } from "./summary.interfaces";
import { startCase } from "lodash";
import { Link as RouterLink } from "react-router-dom";
import { getAlertsAlertPath } from "../../../utils/routes/routes.util";
import AnalysisPeriod from "../anaylysis-period/analysis-period.component";
import { useStyles } from "./summary.styles";

const Summary = ({
    summaryData,
    verboseSummaryItems,
    selectedAnalysisPeriod,
    onAnalysisPeriodChange,
    analysisPeriods,
}: SummaryProps): JSX.Element => {
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

    return (
        <>
            <div className={componentStyles.sectionHeading}>
                <Typography>System stats</Typography>
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
            <Box
                border={1}
                borderColor="grey.500"
                borderRadius={16}
                paddingBottom={1}
                paddingLeft={2}
                paddingRight={2}
                paddingTop={1}
            >
                <Grid container>
                    <Grid item md={11} xs={11}>
                        <Typography variant="h6">Summary</Typography>
                    </Grid>
                    <Grid item alignItems="flex-end" md={1} xs={1}>
                        Copy
                    </Grid>
                    <Grid item md={12} xs={12}>
                        <p>
                            In the last <b>{verboseSummaryItems.weeks} weeks</b>
                            ,
                            <b>
                                {summaryData.anomalies.detected} anomlaies were
                                detected
                            </b>
                            , which is{" "}
                            <b>{verboseSummaryItems.percentageChange}</b> than
                            the previous {verboseSummaryItems.weeks} weeks.{" "}
                            {summaryData.anomalies.detected > 0 && (
                                <>
                                    Notifications about anomallies were sent via
                                    Slack and Email.
                                </>
                            )}
                        </p>
                        {verboseSummaryItems.topAlert.name && (
                            <p>
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
                                        verboseSummaryItems.topAlert
                                            .anomaliesCount
                                    }{" "}
                                    anomalies were detected on this metric.
                                </b>
                            </p>
                        )}
                        <p>
                            <b>
                                {verboseSummaryItems.investigation.count}{" "}
                                investigations
                            </b>{" "}
                            were performed in the last{" "}
                            {verboseSummaryItems.weeks} weeks.{" "}
                            {verboseSummaryItems.investigation.count > 0 && (
                                <span>
                                    The most recent investigation was performed
                                    for an anomaly that happened on{" "}
                                    <b>
                                        {verboseSummaryItems.investigation.date}
                                    </b>{" "}
                                    on metric{" "}
                                    {
                                        verboseSummaryItems.investigation
                                            .anomaly.name
                                    }
                                </span>
                            )}
                        </p>
                    </Grid>
                </Grid>
            </Box>
        </>
    );
};

export default Summary;
