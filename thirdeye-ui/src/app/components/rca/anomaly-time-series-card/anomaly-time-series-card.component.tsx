import { Box, Card, CardContent, Grid } from "@material-ui/core";
import { isEmpty } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useSearchParams } from "react-router-dom";
import {
    AppLoadingIndicatorV1,
    NotificationTypeV1,
    useNotificationProviderV1,
} from "../../../platform/components";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { useGetEvaluation } from "../../../rest/alerts/alerts.actions";
import { getAlertEvaluation } from "../../../rest/alerts/alerts.rest";
import { AlertEvaluation } from "../../../rest/dto/alert.interfaces";
import { createAlertEvaluation } from "../../../utils/anomalies/anomalies.util";
import { AnomalyFilterOption } from "../../anomaly-dimension-analysis/anomaly-dimension-analysis.interfaces";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { TimeRangeQueryStringKey } from "../../time-range/time-range-provider/time-range-provider.interfaces";
import { TimeSeriesChart } from "../../visualizations/time-series-chart/time-series-chart.component";
import { AnomalyTimeSeriesCardProps } from "./anomaly-time-series-card.interfaces";
import { generateChartOptions } from "./anomaly-time-series-card.utils";
import { FiltersSetTable } from "./filters-set-table/filters-set-table.component";

export const AnomalyTimeSeriesCard: FunctionComponent<
    AnomalyTimeSeriesCardProps
> = ({ anomaly, timeSeriesFiltersSet, onRemoveBtnClick }) => {
    const [searchParams] = useSearchParams();
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
    const startTsStr = searchParams.get(TimeRangeQueryStringKey.START_TIME);
    const endTsStr = searchParams.get(TimeRangeQueryStringKey.END_TIME);

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
            const filters = filterSet.map(
                (filter) => `${filter.key}=${filter.value}`
            );

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
        fetchAlertEvaluation();
    }, [anomaly, searchParams]);

    useEffect(() => {
        fetchFilteredAlertEvaluations();
    }, [anomaly, searchParams, timeSeriesFiltersSet]);

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

    return (
        <Card variant="outlined">
            {getEvaluationRequestStatus === ActionStatus.Working && (
                <Card variant="outlined">
                    <CardContent>
                        <Box pb={20} pt={20}>
                            <AppLoadingIndicatorV1 />
                        </Box>
                    </CardContent>
                </Card>
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
