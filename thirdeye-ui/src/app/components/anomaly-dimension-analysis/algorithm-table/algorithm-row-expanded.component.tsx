import { Box, Grid } from "@material-ui/core";
import { isEmpty } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useSearchParams } from "react-router-dom";
import {
    AppLoadingIndicatorV1,
    NotificationTypeV1,
    useNotificationProviderV1,
} from "../../../platform/components";
import { formatLargeNumberV1 } from "../../../platform/utils";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { useGetEvaluation } from "../../../rest/alerts/alerts.actions";
import { createAlertEvaluation } from "../../../utils/anomalies/anomalies.util";
import { useCommonStyles } from "../../../utils/material-ui/common.styles";
import { NoDataIndicator } from "../../no-data-indicator/no-data-indicator.component";
import { TimeRangeQueryStringKey } from "../../time-range/time-range-provider/time-range-provider.interfaces";
import { AlgorithmRowExpandedProps } from "./algorithm-table.interfaces";
import {
    SERVER_VALUE_ALL_VALUES,
    SERVER_VALUE_FOR_OTHERS,
} from "./algorithm-table.utils";

export const AlgorithmRowExpanded: FunctionComponent<
    AlgorithmRowExpandedProps
> = ({ row, anomaly, dimensionColumns }) => {
    const commonClasses = useCommonStyles();
    const {
        evaluation: nonFilteredEvaluationData,
        getEvaluation: getNonFilteredEvaluation,
        errorMessages: getNonFilteredEvaluationErrorMessages,
        status: getNonFilteredEvaluationStatus,
    } = useGetEvaluation();
    const {
        evaluation: filteredEvaluationData,
        getEvaluation: getFilteredEvaluation,
        errorMessages: getFilteredEvaluationErrorMessages,
        status: getFilteredEvaluationStatus,
    } = useGetEvaluation();
    const [searchParams] = useSearchParams();
    const [chartDataIsLoading, setChartDataIsLoading] = useState(true);
    const [chartDataIsHasError, setChartDataIsHasError] = useState(false);
    const { notify } = useNotificationProviderV1();
    const { t } = useTranslation();
    const deviation = row.currentValue - row.baselineValue;
    const hasNegativeDeviation = deviation < 0;

    useEffect(() => {
        fetchAlertEvaluation();
    }, [anomaly, searchParams]);

    useEffect(() => {
        if (
            getFilteredEvaluationStatus !== ActionStatus.Working &&
            getNonFilteredEvaluationStatus !== ActionStatus.Working
        ) {
            console.log(filteredEvaluationData, nonFilteredEvaluationData);
            setChartDataIsLoading(false);
        } else {
            setChartDataIsLoading(true);
        }

        if (
            getFilteredEvaluationStatus === ActionStatus.Error ||
            getNonFilteredEvaluationStatus === ActionStatus.Error
        ) {
            if (getNonFilteredEvaluationStatus === ActionStatus.Error) {
                !isEmpty(getNonFilteredEvaluationErrorMessages)
                    ? getNonFilteredEvaluationErrorMessages.map((msg) =>
                          notify(NotificationTypeV1.Error, msg)
                      )
                    : notify(
                          NotificationTypeV1.Error,
                          t("message.error-while-fetching", {
                              entity: t(
                                  "label.dimension-analysis-row-chart-data"
                              ),
                          })
                      );
            }
            if (getFilteredEvaluationStatus === ActionStatus.Error) {
                !isEmpty(getFilteredEvaluationErrorMessages)
                    ? getFilteredEvaluationErrorMessages.map((msg) =>
                          notify(NotificationTypeV1.Error, msg)
                      )
                    : notify(
                          NotificationTypeV1.Error,
                          t("message.error-while-fetching", {
                              entity: t(
                                  "label.dimension-analysis-row-chart-data"
                              ),
                          })
                      );
            }
            setChartDataIsHasError(true);
        } else {
            setChartDataIsHasError(false);
        }
    }, [getNonFilteredEvaluationStatus, getFilteredEvaluationStatus]);

    const fetchAlertEvaluation = (): void => {
        const start = searchParams.get(TimeRangeQueryStringKey.START_TIME);
        const end = searchParams.get(TimeRangeQueryStringKey.END_TIME);
        const filters: string[] = [];

        row.names.forEach((dimensionValue, idx) => {
            if (dimensionValue === SERVER_VALUE_FOR_OTHERS) {
                row.otherDimensionValues.forEach((otherValue) => {
                    filters.push(`${dimensionColumns[idx]}=${otherValue}`);
                });
            } else if (dimensionValue !== SERVER_VALUE_ALL_VALUES) {
                // (All) means no filter on the column
                filters.push(`${dimensionColumns[idx]}=${dimensionValue}`);
            }
        });

        getNonFilteredEvaluation(
            createAlertEvaluation(anomaly.alert.id, Number(start), Number(end))
        );

        getFilteredEvaluation(
            createAlertEvaluation(anomaly.alert.id, Number(start), Number(end)),
            filters
        );
    };

    return (
        <>
            <Grid container>
                <Grid item md={9} sm={7} xs={12}>
                    {chartDataIsLoading && <AppLoadingIndicatorV1 />}
                    {chartDataIsHasError && <NoDataIndicator />}
                </Grid>
                <Grid item md={3} sm={5} xs={12}>
                    <Box padding="5px">
                        <Grid container>
                            <Grid item xs={12}>
                                <Grid container justifyContent="space-between">
                                    <Grid item>
                                        <Box component="div" textAlign="right">
                                            <strong>Baseline:</strong>
                                        </Box>
                                    </Grid>
                                    <Grid item>
                                        <Box component="div" textAlign="right">
                                            {row.baselineValue}
                                        </Box>
                                    </Grid>
                                </Grid>
                            </Grid>
                            <Grid item xs={12}>
                                <Grid container justifyContent="space-between">
                                    <Grid item>
                                        <Box component="div" textAlign="right">
                                            <strong>Current:</strong>
                                        </Box>
                                    </Grid>
                                    <Grid item>
                                        <Box component="div" textAlign="right">
                                            {row.currentValue}
                                        </Box>
                                    </Grid>
                                </Grid>
                            </Grid>
                            <Grid item xs={12}>
                                <Grid container justifyContent="space-between">
                                    <Grid item>
                                        <Box component="div" textAlign="right">
                                            <strong>% Change:</strong>
                                        </Box>
                                    </Grid>
                                    <Grid item>
                                        <Box
                                            className={
                                                hasNegativeDeviation
                                                    ? commonClasses.decreased
                                                    : commonClasses.increased
                                            }
                                            component="div"
                                            textAlign="right"
                                        >
                                            {row.changePercentage === "NaN" && (
                                                <span>-</span>
                                            )}
                                            {row.changePercentage !== "NaN" && (
                                                <>
                                                    <Box
                                                        display="inline"
                                                        marginRight="5px"
                                                    >
                                                        <span>
                                                            (
                                                            {formatLargeNumberV1(
                                                                row.changePercentage as number
                                                            )}
                                                            %)
                                                        </span>
                                                    </Box>
                                                    <Box display="inline">
                                                        <span>{deviation}</span>
                                                    </Box>
                                                </>
                                            )}
                                        </Box>
                                    </Grid>
                                </Grid>
                            </Grid>
                        </Grid>
                    </Box>
                </Grid>
            </Grid>
        </>
    );
};
