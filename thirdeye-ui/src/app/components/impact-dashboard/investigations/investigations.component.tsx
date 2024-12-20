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

// Components
import { AnalysisPeriod } from "../common/anaylysis-period/analysis-period.component";
import { SectionHeader } from "../common/section-header/section-header.component";
import { Link as RouterLink } from "react-router-dom";
import { Box, Grid, Link, Typography } from "@material-ui/core";

// Styles
import { useInvestigationStyles } from "./investigations.styles";

// Interfaces
import { RecentInvestigationsProps } from "./investigations.interfaces";

// Utils
import { epochToDate } from "../detection-performance/util";
import { compact, isEmpty, uniq } from "lodash";
import { getRootCauseAnalysisForAnomalyInvestigateV2StepsPath } from "../../../utils/routes/routes.util";
import { getFeedbackClass, useGetFeedbackText } from "./util";

// APIs
import { getAnomaly } from "../../../rest/anomalies/anomalies.rest";
import { Anomaly } from "../../../rest/dto/anomaly.interfaces";

export const RecentInvestigations = ({
    investigations,
    analysisPeriods,
    selectedAnalysisPeriod,
    onAnalysisPeriodChange,
}: RecentInvestigationsProps): ReactElement => {
    const { t } = useTranslation();
    const getFeedbackText = useGetFeedbackText();
    const componentStyles = useInvestigationStyles();
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

    // Anomaly feedback is only returned in indivudual details call of the anomaly.
    // Hence we have to make those calls here.
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

    const renderLabel = (labelKey: string): JSX.Element => {
        return (
            <Typography className={componentStyles.label} variant="body2">
                {t(
                    `pages.impact-dashboard.sections.investigations.labels.${labelKey}`
                )}
            </Typography>
        );
    };

    return (
        <>
            <div className={componentStyles.sectionHeading}>
                <SectionHeader
                    heading={t(
                        "pages.impact-dashboard.sections.investigations.heading"
                    )}
                />
                <AnalysisPeriod
                    analysisPeriods={analysisPeriods}
                    selectedPeriod={selectedAnalysisPeriod}
                    onClick={onAnalysisPeriodChange}
                />
            </div>
            {isEmpty(investigations) && (
                <Box
                    border={1}
                    borderColor="grey.500"
                    padding={2}
                    sx={{
                        display: "flex",
                        justifyContent: "center",
                        alignItems: "center",
                    }}
                >
                    No Investigations conducted in the given period.
                </Box>
            )}
            {!isEmpty(investigations) && (
                <Box border={1} borderColor="grey.500" borderRadius={16}>
                    {investigations
                        ?.sort((a, b) => b.created - a.created)
                        .map((investigation, idx) => {
                            return (
                                <Box
                                    borderBottom={
                                        idx !== investigations.length - 1
                                            ? 1
                                            : 0
                                    }
                                    borderColor="grey.500"
                                    key={investigation.id}
                                    padding={2}
                                >
                                    <Grid container key={investigation.id}>
                                        <Grid item sm={3}>
                                            {renderLabel("investigation-name")}
                                            {investigation.anomaly?.id ? (
                                                <Link
                                                    component={RouterLink}
                                                    to={getRootCauseAnalysisForAnomalyInvestigateV2StepsPath(
                                                        investigation.anomaly
                                                            ?.id,
                                                        "what-where-page"
                                                    )}
                                                >
                                                    {investigation.name}
                                                </Link>
                                            ) : (
                                                <div>{investigation.name}</div>
                                            )}
                                        </Grid>
                                        <Grid item sm={3}>
                                            {renderLabel("created-date")}
                                            <div>
                                                {epochToDate(
                                                    investigation.created
                                                )}
                                            </div>
                                        </Grid>
                                        <Grid item sm={3}>
                                            {renderLabel("created-by")}
                                            <div>
                                                {
                                                    investigation.createdBy
                                                        .principal
                                                }
                                            </div>
                                        </Grid>
                                        <Grid item sm={3}>
                                            {renderLabel(
                                                "anomaly-confirmation"
                                            )}
                                            <div
                                                className={
                                                    investigation.anomaly?.id
                                                        ? componentStyles[
                                                              getFeedbackClass(
                                                                  anomaliesFeedback[
                                                                      investigation
                                                                          .anomaly
                                                                          ?.id
                                                                  ]
                                                              )
                                                          ]
                                                        : ""
                                                }
                                            >
                                                {investigation.anomaly?.id
                                                    ? getFeedbackText(
                                                          anomaliesFeedback[
                                                              investigation
                                                                  .anomaly?.id
                                                          ]
                                                      )
                                                    : ""}
                                            </div>
                                        </Grid>
                                        <Grid item sm={3}>
                                            <div>
                                                {investigation.uiMetadata
                                                    ?.eventSet &&
                                                    t(
                                                        "pages.impact-dashboard.sections.investigations.labels.related-event"
                                                    )}
                                            </div>
                                            <ul>
                                                {investigation.uiMetadata?.eventSet?.map(
                                                    (event) => {
                                                        return (
                                                            <li
                                                                key={event.name}
                                                            >
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
            )}
        </>
    );
};
