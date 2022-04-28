import {
    Box,
    Button,
    ButtonGroup,
    Card,
    CardContent,
    CardHeader,
    Grid,
} from "@material-ui/core";
import { isEmpty } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useSearchParams } from "react-router-dom";
import {
    AppLoadingIndicatorV1,
    NotificationTypeV1,
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
import { TimeRangeQueryStringKey } from "../../time-range/time-range-provider/time-range-provider.interfaces";
import { TimeSeriesChart } from "../../visualizations/time-series-chart/time-series-chart.component";
import { AnomalyTimeSeriesCardProps } from "./anomaly-time-series-card.interfaces";
import { generateChartOptions } from "./anomaly-time-series-card.utils";
import { FiltersSetTable } from "./filters-set-table/filters-set-table.component";

const CHART_HEIGHT_KEY = "chartHeight";
const CHART_SIZE_OPTIONS = [
    ["S", 500],
    ["M", 800],
    ["L", 1100],
];

export const AnomalyTimeSeriesCard: FunctionComponent<
    AnomalyTimeSeriesCardProps
> = ({ anomaly, timeSeriesFiltersSet, onRemoveBtnClick }) => {
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
    const [startTsStr, setStartTsStr] = useState<string | null>(
        searchParams.get(TimeRangeQueryStringKey.START_TIME)
    );
    const [endTsStr, setEndTsStr] = useState<string | null>(
        searchParams.get(TimeRangeQueryStringKey.END_TIME)
    );
    const [chartHeight, setChartHeight] = useState<number>(
        searchParams.get(CHART_HEIGHT_KEY) !== null
            ? Number(searchParams.get(CHART_HEIGHT_KEY))
            : 500
    );

    const alertEvaluationPayload = createAlertEvaluation(
        anomaly.alert.id,
        Number(startTsStr),
        Number(endTsStr)
    );

    const fetchAlertEvaluation = (): void => {
        setAlertEvaluation(null);

        if (!anomaly || !anomaly.alert || !startTsStr || !endTsStr) {
            return;
        }

        getEvaluation(alertEvaluationPayload).then(
            (fetchedEvaluation) =>
                fetchedEvaluation && setAlertEvaluation(fetchedEvaluation)
        );
    };

    const fetchFilteredAlertEvaluations = (): void => {
        setFilteredAlertEvaluation([]);

        if (!anomaly || !anomaly.alert || !startTsStr || !endTsStr) {
            return;
        }

        const dataRequests = timeSeriesFiltersSet.map((filterSet) => {
            const filters = filterSet.map(concatKeyValueWithEqual);

            return getAlertEvaluation(alertEvaluationPayload, filters);
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
        /**
         * Ensure the chart data only refreshes when the start and end from the
         * query params change
         */
        setStartTsStr(searchParams.get(TimeRangeQueryStringKey.START_TIME));
        setEndTsStr(searchParams.get(TimeRangeQueryStringKey.END_TIME));
    }, [searchParams]);

    useEffect(() => {
        fetchAlertEvaluation();
    }, [anomaly]);

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

    return (
        <Card variant="outlined">
            <CardHeader
                action={
                    <>
                        <TooltipV1
                            placement="top"
                            title={t("message.set-chart-height")}
                        >
                            <ButtonGroup
                                color="secondary"
                                size="small"
                                variant="outlined"
                            >
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
                    </>
                }
            />
            {getEvaluationRequestStatus === ActionStatus.Working && (
                <CardContent>
                    <Box pb={20} pt={20}>
                        <AppLoadingIndicatorV1 />
                    </Box>
                </CardContent>
            )}
            {getEvaluationRequestStatus === ActionStatus.Error && (
                <CardContent>
                    <Box pb={20} pt={20}>
                        <NoDataIndicator />
                    </Box>
                </CardContent>
            )}
            {getEvaluationRequestStatus === ActionStatus.Done &&
                alertEvaluation !== null &&
                timeSeriesFiltersSet.length === 0 && (
                    <CardContent>
                        <TimeSeriesChart
                            height={chartHeight}
                            {...generateChartOptions(
                                alertEvaluation,
                                anomaly,
                                filteredAlertEvaluation,
                                t
                            )}
                        />
                    </CardContent>
                )}
            {getEvaluationRequestStatus === ActionStatus.Done &&
                alertEvaluation !== null &&
                timeSeriesFiltersSet.length > 0 && (
                    <CardContent>
                        <Grid container>
                            <Grid item md={8} sm={12} xs={12}>
                                <TimeSeriesChart
                                    height={chartHeight}
                                    {...generateChartOptions(
                                        alertEvaluation,
                                        anomaly,
                                        filteredAlertEvaluation,
                                        t
                                    )}
                                />
                            </Grid>
                            <Grid item md={4} sm={12} xs={12}>
                                <FiltersSetTable
                                    timeSeriesFiltersSet={timeSeriesFiltersSet}
                                    onRemoveBtnClick={onRemoveBtnClick}
                                />
                            </Grid>
                        </Grid>
                    </CardContent>
                )}
        </Card>
    );
};
