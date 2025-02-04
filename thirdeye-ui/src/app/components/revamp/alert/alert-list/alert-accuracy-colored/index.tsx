/*
 * Copyright 2025 StarTree Inc
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
import React, { FunctionComponent, useEffect, useMemo } from "react";
import { SkeletonV1, TooltipV1 } from "../../../../../platform/components";
import { Typography, useTheme } from "@material-ui/core";
import { LoadingErrorStateSwitch } from "../../../../page-states/loading-error-state-switch/loading-error-state-switch.component";
import { getAlertAccuracyData } from "../../../../../utils/alerts/alerts.util";
import { AlertAccuracyColoredProps } from "./interface";
import { useInView } from "react-intersection-observer";
import { useTranslation } from "react-i18next";
import { capitalize } from "lodash";
import { AnomalyFeedbackType } from "../../../../../rest/dto/anomaly.interfaces";

export const AlertAccuracyColored: FunctionComponent<AlertAccuracyColoredProps> =
    ({
        typographyProps,
        label,
        defaultSkeletonProps,
        alertStats,
        triggerViewCallback,
    }) => {
        const theme = useTheme();
        const { t } = useTranslation();

        const { ref, inView } = useInView({
            triggerOnce: true,
            delay: 250,
            threshold: 1,
        });

        useEffect(() => {
            if (inView && triggerViewCallback) {
                triggerViewCallback();
            }
        }, [inView]);

        const { noAnomalyData, typographyColor, accuracyString } =
            useMemo(() => {
                if (alertStats) {
                    const { accuracy, colorScheme, noAnomalyData } =
                        getAlertAccuracyData(alertStats);

                    const accuracyString = `${(100 * accuracy).toFixed(2)}%`;

                    const color = theme.palette[colorScheme].main;

                    return {
                        noAnomalyData,
                        typographyColor: !noAnomalyData ? { color } : undefined,
                        accuracyString,
                    };
                }

                return {
                    noAnomalyData: undefined,
                    typographyColor: undefined,
                    accuracyString: undefined,
                };
            }, [alertStats]);

        let displayValue: string | undefined = capitalize(
            t("message.no-entity-data", {
                entity: t("label.anomaly"),
            })
        );

        if (!noAnomalyData) {
            if (label) {
                displayValue = `${label}: ${accuracyString}`;
            } else {
                displayValue = accuracyString;
            }
        }

        return (
            <TooltipV1
                delay={50}
                placement="bottom"
                title={
                    !!alertStats && (
                        <table>
                            <tbody>
                                <tr>
                                    <td>
                                        {t("message.total-reported-anomalies")}
                                    </td>
                                    <td>{alertStats?.stats?.totalCount}</td>
                                </tr>
                                <tr>
                                    <td>
                                        {t("message.anomalies-with-feedback")}
                                    </td>
                                    <td>
                                        {alertStats?.stats?.countWithFeedback}
                                    </td>
                                </tr>
                                <tr>
                                    <td>
                                        {t("message.misreported-anomalies")}
                                    </td>
                                    <td>
                                        {
                                            alertStats?.stats?.feedbackStats[
                                                AnomalyFeedbackType.NOT_ANOMALY
                                            ]
                                        }
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    )
                }
            >
                <div>
                    <Typography
                        color="secondary"
                        innerRef={ref}
                        style={{
                            ...typographyColor,
                            ...(typographyProps?.color
                                ? { color: typographyProps.color }
                                : {}),
                        }}
                        variant="body1"
                        {...typographyProps}
                    >
                        <LoadingErrorStateSwitch
                            isError={false}
                            isLoading={alertStats?.loading}
                            loadingState={
                                <SkeletonV1
                                    width={50}
                                    {...defaultSkeletonProps}
                                />
                            }
                        >
                            <>{displayValue}</>
                        </LoadingErrorStateSwitch>
                    </Typography>
                </div>
            </TooltipV1>
        );
    };
