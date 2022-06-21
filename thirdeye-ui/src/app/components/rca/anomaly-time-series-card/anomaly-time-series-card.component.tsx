/**
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
import {
    Box,
    Button,
    ButtonGroup,
    Card,
    CardContent,
    Grid,
} from "@material-ui/core";
import { debounce, isEmpty } from "lodash";
import React, {
    FunctionComponent,
    useCallback,
    useEffect,
    useMemo,
    useState,
} from "react";
import { useTranslation } from "react-i18next";
import { useSearchParams } from "react-router-dom";
import {
    NotificationTypeV1,
    PageContentsCardV1,
    SkeletonV1,
    TooltipV1,
    useNotificationProviderV1,
} from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { useGetEvaluation } from "../../../rest/alerts/alerts.actions";
import { getAlertEvaluation } from "../../../rest/alerts/alerts.rest";
import { AlertEvaluation } from "../../../rest/dto/alert.interfaces";
import { createAlertEvaluation } from "../../../utils/anomalies/anomalies.util";
import { concatKeyValueWithEqual } from "../../../utils/params/params.util";
import { AnomalyFilterOption } from "../../anomaly-dimension-analysis/anomaly-dimension-analysis.interfaces";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { TimeRangeButtonWithContext } from "../../time-range/time-range-button-with-context/time-range-button.component";
import { TimeRangeQueryStringKey } from "../../time-range/time-range-provider/time-range-provider.interfaces";
import { TimeSeriesChart } from "../../visualizations/time-series-chart/time-series-chart.component";
import { ZoomDomain } from "../../visualizations/time-series-chart/time-series-chart.interfaces";
import { AnomalyTimeSeriesCardProps } from "./anomaly-time-series-card.interfaces";
import {
    determineInitialZoom,
    generateChartOptions,
    ZOOM_END_KEY,
    ZOOM_START_KEY,
} from "./anomaly-time-series-card.utils";
import { RCAChartLegend } from "./rca-chart-legend/rca-chart-legend.component";

const CHART_HEIGHT_KEY = "chartHeight";
const CHART_SIZE_OPTIONS = [
    ["S", 500],
    ["M", 800],
    ["L", 1100],
];

export const AnomalyTimeSeriesCard: FunctionComponent<
    AnomalyTimeSeriesCardProps
> = ({
    anomaly,
    timeSeriesFiltersSet,
    onRemoveBtnClick,
    events,
    isLoading,
    onEventSelectionChange,
}) => {
    const [searchParams, setSearchParams] = useSearchParams();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();

    const {
        getEvaluation,
        errorMessages,
        status: getEvaluationRequestStatus,
    } = useGetEvaluation();
    const [alertEvaluation, setAlertEvaluation] =
        useState<AlertEvaluation | null>(null);
    const [filteredAlertEvaluation, setFilteredAlertEvaluation] = useState<
        [AlertEvaluation, AnomalyFilterOption[]][]
    >([]);
    const [startTime, endTime] = useMemo(
        () => [
            Number(searchParams.get(TimeRangeQueryStringKey.START_TIME)),
            Number(searchParams.get(TimeRangeQueryStringKey.END_TIME)),
        ],
        [searchParams]
    );
    const [chartHeight, setChartHeight] = useState<number>(
        searchParams.get(CHART_HEIGHT_KEY) !== null
            ? Number(searchParams.get(CHART_HEIGHT_KEY))
            : 500
    );

    const [initialZoom, setInitialZoom] = useState<ZoomDomain | undefined>(
        determineInitialZoom(searchParams)
    );

    const fetchAlertEvaluation = (): void => {
        setAlertEvaluation(null);

        if (!anomaly || !anomaly.alert || !startTime || !endTime) {
            return;
        }

        getEvaluation(
            createAlertEvaluation(anomaly.alert.id, startTime, endTime)
        ).then(
            (fetchedEvaluation) =>
                fetchedEvaluation && setAlertEvaluation(fetchedEvaluation)
        );
    };

    const fetchFilteredAlertEvaluations = (): void => {
        setFilteredAlertEvaluation([]);

        if (!anomaly || !anomaly.alert || !startTime || !endTime) {
            return;
        }

        const dataRequests = timeSeriesFiltersSet.map((filterSet) => {
            const filters = filterSet.map(concatKeyValueWithEqual);

            return getAlertEvaluation(
                createAlertEvaluation(anomaly.alert.id, startTime, endTime),
                filters
            );
        });

        Promise.all(dataRequests).then((dataFromRequests) => {
            setFilteredAlertEvaluation(
                dataFromRequests.map((alertEval, idx) => {
                    return [alertEval, timeSeriesFiltersSet[idx]];
                })
            );
        });
    };

    useEffect(() => {
        fetchAlertEvaluation();
    }, [anomaly, startTime, endTime]);

    useEffect(() => {
        fetchFilteredAlertEvaluations();
    }, [anomaly, timeSeriesFiltersSet]);

    useEffect(() => {
        if (getEvaluationRequestStatus === ActionStatus.Error) {
            !isEmpty(errorMessages)
                ? errorMessages.map((msg) =>
                      notify(NotificationTypeV1.Error, msg)
                  )
                : notify(
                      NotificationTypeV1.Error,
                      t("message.error-while-fetching", {
                          entity: t("label.chart-data"),
                      })
                  );
        }
    }, [errorMessages, getEvaluationRequestStatus]);

    const handleChartHeightChange = (height: number): void => {
        setChartHeight(height);
        searchParams.set(CHART_HEIGHT_KEY, height.toString());
        setSearchParams(searchParams);
    };

    const debouncedChangeSearchParamsForDomain = useCallback(
        debounce(
            (
                domain: ZoomDomain | null,
                searchParamsProp: URLSearchParams,
                setSearchParamFunc: (
                    newSearchParams: URLSearchParams,
                    options: { replace: boolean }
                ) => void
            ) => {
                setInitialZoom(domain ?? undefined);
                if (domain) {
                    searchParamsProp.set(ZOOM_START_KEY, domain.x0.toString());
                    searchParamsProp.set(ZOOM_END_KEY, domain.x1.toString());
                } else {
                    searchParamsProp.delete(ZOOM_START_KEY);
                    searchParamsProp.delete(ZOOM_END_KEY);
                }
                setSearchParamFunc(searchParamsProp, { replace: true });
            },
            250
        ),
        []
    );

    const handleZoomChange = (domain: ZoomDomain | null): void => {
        debouncedChangeSearchParamsForDomain(
            domain,
            searchParams,
            setSearchParams
        );
    };

    if (isLoading) {
        return (
            <PageContentsCardV1>
                <SkeletonV1 height={400} variant="rect" />
            </PageContentsCardV1>
        );
    }

    return (
        <Card variant="outlined">
            <CardContent>
                <Grid container justifyContent="flex-end">
                    <Grid item>
                        <TimeRangeButtonWithContext />
                    </Grid>
                    <Grid item>
                        <TooltipV1
                            placement="top"
                            title={t("message.set-chart-height")}
                        >
                            <ButtonGroup color="secondary" variant="outlined">
                                {CHART_SIZE_OPTIONS.map((sizeOption) => (
                                    <Button
                                        disabled={chartHeight === sizeOption[1]}
                                        key={sizeOption[0]}
                                        onClick={() =>
                                            handleChartHeightChange(
                                                sizeOption[1] as number
                                            )
                                        }
                                    >
                                        {sizeOption[0]}
                                    </Button>
                                ))}
                            </ButtonGroup>
                        </TooltipV1>
                    </Grid>
                </Grid>
            </CardContent>
            {getEvaluationRequestStatus === ActionStatus.Working && (
                <CardContent>
                    <SkeletonV1 height={350} variant="rect" />
                </CardContent>
            )}
            {getEvaluationRequestStatus === ActionStatus.Error && (
                <CardContent>
                    <Box pb={20} pt={20}>
                        <NoDataIndicator />
                    </Box>
                </CardContent>
            )}
            {anomaly &&
                getEvaluationRequestStatus === ActionStatus.Done &&
                alertEvaluation !== null && (
                    <CardContent>
                        <TimeSeriesChart
                            events={events}
                            height={chartHeight}
                            {...generateChartOptions(
                                alertEvaluation,
                                anomaly,
                                filteredAlertEvaluation,
                                t
                            )}
                            LegendComponent={(props) => (
                                <RCAChartLegend
                                    {...props}
                                    timeSeriesFiltersSet={timeSeriesFiltersSet}
                                    onEventSelectionChange={
                                        onEventSelectionChange
                                    }
                                    onRemoveBtnClick={onRemoveBtnClick}
                                />
                            )}
                            chartEvents={{
                                onZoomChange: handleZoomChange,
                            }}
                            initialZoom={initialZoom}
                        />
                    </CardContent>
                )}
        </Card>
    );
};
