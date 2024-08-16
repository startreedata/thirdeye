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
import AnalysisPeriod from "../anaylysis-period/analysis-period.component";
import { Box, Grid, Typography } from "@material-ui/core";
import { useStyles } from "./investigations.styles";
import { RecentInvestigationsProps } from "./investigations.interfaces";
import { epochToDate } from "../detection-performance/util";
import { compact, isEmpty, uniq } from "lodash";
import { getAnomaly } from "../../../rest/anomalies/anomalies.rest";
import { Anomaly } from "../../../rest/dto/anomaly.interfaces";

const getFeedbackText = (feedback: string | undefined): string | undefined => {
    if (feedback === "ANOMALY") {
        return "Yes, this is a valid anomaly";
    } else if (feedback === "NOT_ANOMALY") {
        return "No, this is not an anomaly";
    } else if (!feedback) {
        return "No feedback present";
    } else {
        return feedback;
    }
};

const getFeedbackClass = (feedback: string | undefined): string => {
    if (feedback === "ANOMALY") {
        return "validAnomaly";
    } else if (feedback === "NOT_ANOMALY") {
        return "invalidAnomaly";
    } else {
        return "text";
    }
};

const RecentInvestigations = ({
    investigations,
    analysisPeriods,
    selectedAnalysisPeriod,
    onAnalysisPeriodChange,
}: RecentInvestigationsProps): ReactElement => {
    const componentStyles = useStyles();
    const [anomaliesInvestigated, setAnomaliesInvestigated] =
        useState<number[]>();
    const [anomaliesFeedback, setAnomaliesFeedback] = useState<{
        [key: number]: string | undefined;
    }>({});

    useEffect(() => {
        if (investigations?.length) {
            const anomaliesId = compact(
                investigations.map((investigation) => investigation.anomaly?.id)
            );
            const uniqueAnomalies = uniq(anomaliesId);
            setAnomaliesInvestigated(uniqueAnomalies);
        }
    }, [investigations]);

    useEffect(() => {
        if (!isEmpty(anomaliesInvestigated)) {
            const anomalyPromises: { id: number; promise: Promise<Anomaly> }[] =
                anomaliesInvestigated!.map((anomalyInvestigated) => ({
                    id: anomalyInvestigated,
                    promise: getAnomaly(anomalyInvestigated),
                }));
            anomalyPromises?.forEach(({ id, promise }) => {
                promise
                    .then((response) => {
                        setAnomaliesFeedback((prevState) => {
                            if (prevState) {
                                return {
                                    ...prevState,
                                    [id]: response.feedback?.type,
                                };
                            } else {
                                return { [id]: response.feedback?.type };
                            }
                        });
                    })
                    .catch(() => {
                        setAnomaliesFeedback((prevState) => {
                            if (prevState) {
                                return {
                                    ...prevState,
                                    [id]: "Error fetching feedback",
                                };
                            } else {
                                return { [id]: "Error fetching feedback" };
                            }
                        });
                    });
            });
        }
    }, [anomaliesInvestigated]);

    return (
        <>
            <div className={componentStyles.sectionHeading}>
                <Typography variant="h6">Recent investigations</Typography>
                <AnalysisPeriod
                    analysisPeriods={analysisPeriods}
                    selectedPeriod={selectedAnalysisPeriod}
                    onClick={onAnalysisPeriodChange}
                />
            </div>
            <Box border={1} borderColor="grey.500" borderRadius={16}>
                {investigations?.map((investigation, idx) => {
                    return (
                        <Box
                            borderBottom={
                                idx !== investigations.length - 1 ? 1 : 0
                            }
                            borderColor="grey.500"
                            key={investigation.id}
                            padding={2}
                        >
                            <Grid container key={investigation.id}>
                                <Grid item sm={3}>
                                    <div>{investigation.name}</div>
                                    <Typography
                                        className={componentStyles.label}
                                        variant="body2"
                                    >
                                        Investigation name
                                    </Typography>
                                </Grid>
                                <Grid item sm={3}>
                                    <div>
                                        {epochToDate(investigation.created)}
                                    </div>
                                    <Typography
                                        className={componentStyles.label}
                                        variant="body2"
                                    >
                                        Created date
                                    </Typography>
                                </Grid>
                                <Grid item sm={3}>
                                    <div>
                                        {investigation.createdBy.principal}
                                    </div>
                                    <Typography
                                        className={componentStyles.label}
                                        variant="body2"
                                    >
                                        Created by
                                    </Typography>
                                </Grid>
                                <Grid item sm={3}>
                                    <div
                                        className={
                                            investigation.anomaly?.id
                                                ? componentStyles[
                                                      getFeedbackClass(
                                                          anomaliesFeedback[
                                                              investigation
                                                                  .anomaly?.id
                                                          ]
                                                      )
                                                  ]
                                                : ""
                                        }
                                    >
                                        {investigation.anomaly?.id
                                            ? getFeedbackText(
                                                  anomaliesFeedback[
                                                      investigation.anomaly?.id
                                                  ]
                                              )
                                            : ""}
                                    </div>
                                    <Typography
                                        className={componentStyles.label}
                                        variant="body2"
                                    >
                                        Anomaly confirmation
                                    </Typography>
                                </Grid>
                                <Grid item sm={3}>
                                    <div>
                                        {investigation.uiMetadata?.eventSet &&
                                            "Anomaly was related to:"}
                                    </div>
                                    <ul>
                                        {investigation.uiMetadata?.eventSet?.map(
                                            (event) => {
                                                return (
                                                    <li key={event.name}>
                                                        {event.name}
                                                    </li>
                                                );
                                            }
                                        )}
                                    </ul>
                                </Grid>
                            </Grid>
                        </Box>
                    );
                })}
            </Box>
        </>
    );
};

export default RecentInvestigations;
