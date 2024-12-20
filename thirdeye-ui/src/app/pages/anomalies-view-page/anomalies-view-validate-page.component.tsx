/*
 * Copyright 2023 StarTree Inc
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
import { Box, Button, Grid } from "@material-ui/core";
import React, { FunctionComponent, useMemo } from "react";
import { useTranslation } from "react-i18next";
import { useOutletContext, useSearchParams } from "react-router-dom";
import { Chart } from "../../components/anomalies-view/chart/chart.component";
import { TimeRangeQueryStringKey } from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import {
    determineTimezoneFromAlertInEvaluation,
    shouldHideTimeInDatetimeFormat,
} from "../../utils/alerts/alerts.util";
import { AnomalyViewContainerPageOutletContext } from "./anomalies-view-page.interfaces";

const ADDITIONAL_CHARTS_QUERY_PARAM_KEY = "timePeriodCharts";

export const AnomaliesViewValidatePage: FunctionComponent = () => {
    const { anomaly, alertInsight } =
        useOutletContext<AnomalyViewContainerPageOutletContext>();
    const [searchParams, setSearchParams] = useSearchParams();
    const { t } = useTranslation();

    // Array of strings in ISO-8601 format
    const additionalCharts: string[] = useMemo(() => {
        if (searchParams.has(ADDITIONAL_CHARTS_QUERY_PARAM_KEY)) {
            try {
                return JSON.parse(
                    searchParams.get(
                        ADDITIONAL_CHARTS_QUERY_PARAM_KEY
                    ) as string
                );
            } catch {
                return [];
            }
        }

        return [];
    }, [searchParams]);

    const [start, end] = useMemo(() => {
        const start = searchParams.get(TimeRangeQueryStringKey.START_TIME);
        const end = searchParams.get(TimeRangeQueryStringKey.END_TIME);

        return [Number(start), Number(end)];
    }, [searchParams]);

    const handleAddChartClick = (): void => {
        const newState = [...additionalCharts];

        // Use the last previous period as the new previous period
        if (newState.length > 0) {
            newState.push(newState[newState.length - 1]);
        } else {
            // Default to no offset from the start and end
            newState.push("P1W");
        }

        searchParams.set(
            ADDITIONAL_CHARTS_QUERY_PARAM_KEY,
            JSON.stringify(newState)
        );
        setSearchParams(searchParams);
    };

    const handleDeleteChartClick = (idx: number): void => {
        const removedElement = [...additionalCharts];

        removedElement.splice(idx, 1);

        if (removedElement.length === 0) {
            searchParams.delete(ADDITIONAL_CHARTS_QUERY_PARAM_KEY);
        } else {
            searchParams.set(
                ADDITIONAL_CHARTS_QUERY_PARAM_KEY,
                JSON.stringify(removedElement)
            );
        }
        setSearchParams(searchParams);
    };

    const handleStartEndChange = (start: number, end: number): void => {
        searchParams.set(TimeRangeQueryStringKey.START_TIME, start.toString());
        searchParams.set(TimeRangeQueryStringKey.END_TIME, end.toString());
        setSearchParams(searchParams);
    };

    const handlePastPeriodChangeForIdx = (
        duration: string,
        idx: number
    ): void => {
        const replaced = [...additionalCharts];
        replaced[idx] = duration;

        searchParams.set(
            ADDITIONAL_CHARTS_QUERY_PARAM_KEY,
            JSON.stringify(replaced)
        );
        setSearchParams(searchParams);
    };

    return (
        <Grid item xs={12}>
            <Grid container spacing={1}>
                <Grid item xs={12}>
                    <Chart
                        anomaly={anomaly}
                        chartHeight={
                            additionalCharts.length === 0 ? 400 : undefined
                        }
                        end={end}
                        hideChartBrush={additionalCharts.length > 0}
                        hideTime={shouldHideTimeInDatetimeFormat(
                            alertInsight?.templateWithProperties
                        )}
                        maxDate={alertInsight?.datasetEndTime}
                        minDate={alertInsight?.datasetStartTime}
                        start={start}
                        timezone={determineTimezoneFromAlertInEvaluation(
                            alertInsight?.templateWithProperties
                        )}
                        onDateChange={handleStartEndChange}
                    />
                </Grid>
                {additionalCharts.map((pastPeriod, idx) => {
                    return (
                        <Grid item key={`${idx}-${pastPeriod}`} xs={12}>
                            <Chart
                                hideChartBrush
                                anomaly={anomaly}
                                end={end}
                                hideTime={shouldHideTimeInDatetimeFormat(
                                    alertInsight?.templateWithProperties
                                )}
                                start={start}
                                startEndShift={pastPeriod}
                                timezone={determineTimezoneFromAlertInEvaluation(
                                    alertInsight?.templateWithProperties
                                )}
                                onDeleteClick={() =>
                                    handleDeleteChartClick(idx)
                                }
                                onPastPeriodChange={(duration) =>
                                    handlePastPeriodChangeForIdx(duration, idx)
                                }
                            />
                        </Grid>
                    );
                })}
                <Grid item xs={12}>
                    <Box textAlign="right">
                        <Button
                            color="primary"
                            data-testid="add-previous-period"
                            style={{ backgroundColor: "#FFF" }}
                            variant="outlined"
                            onClick={handleAddChartClick}
                        >
                            {t("label.add-previous-period-to-compare")}
                        </Button>
                    </Box>
                </Grid>
            </Grid>
        </Grid>
    );
};
