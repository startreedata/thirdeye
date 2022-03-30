import { Box, Card, CardContent, Grid, Paper } from "@material-ui/core";
import { isEmpty, toNumber } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams, useSearchParams } from "react-router-dom";
import { AnomalyFeedback } from "../../components/anomlay-feedback/anomaly-feedback.component";
import { AnomalySummaryCard } from "../../components/entity-cards/root-cause-analysis/anomaly-summary-card/anomaly-summary-card.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { AnalysisTabs } from "../../components/rca/analysis-tabs/analysis-tabs.component";
import { TimeRangeQueryStringKey } from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import { AlertEvaluationTimeSeriesCard } from "../../components/visualizations/alert-evaluation-time-series-card/alert-evaluation-time-series-card.component";
import {
    AppLoadingIndicatorV1,
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetEvaluation } from "../../rest/alerts/alerts.actions";
import { useGetAnomaly } from "../../rest/anomalies/anomaly.actions";
import { AlertEvaluation } from "../../rest/dto/alert.interfaces";
import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";
import { DEFAULT_FEEDBACK } from "../../utils/alerts/alerts.util";
import {
    createAlertEvaluation,
    getUiAnomaly,
} from "../../utils/anomalies/anomalies.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { RootCauseAnalysisForAnomalyPageParams } from "./root-cause-analysis-for-anomaly-page.interfaces";
import { useRootCauseAnalysisForAnomalyPageStyles } from "./root-cause-analysis-for-anomaly-page.style";

export const RootCauseAnalysisForAnomalyPage: FunctionComponent = () => {
    const {
        anomaly,
        getAnomaly,
        status: getAnomalyRequestStatus,
        errorMessages: anomalyRequestErrors,
    } = useGetAnomaly();
    const {
        evaluation,
        getEvaluation,
        errorMessages,
        status: getEvaluationRequestStatus,
    } = useGetEvaluation();
    const [uiAnomaly, setUiAnomaly] = useState<UiAnomaly | null>(null);
    const [alertEvaluation, setAlertEvaluation] =
        useState<AlertEvaluation | null>(null);
    const { notify } = useNotificationProviderV1();
    const { id: anomalyId } =
        useParams<RootCauseAnalysisForAnomalyPageParams>();
    const { t } = useTranslation();
    const [searchParams] = useSearchParams();
    const style = useRootCauseAnalysisForAnomalyPageStyles();

    const pageTitle = `${t("label.root-cause-analysis")}: ${t(
        "label.anomaly"
    )} #${anomalyId}`;

    useEffect(() => {
        !!anomalyId &&
            isValidNumberId(anomalyId) &&
            getAnomaly(toNumber(anomalyId));
    }, [anomalyId]);

    useEffect(() => {
        !!anomaly && setUiAnomaly(getUiAnomaly(anomaly));
    }, [anomaly]);

    useEffect(() => {
        fetchAlertEvaluation();
    }, [uiAnomaly, searchParams]);

    useEffect(() => {
        if (evaluation && anomaly) {
            const anomaliesDetector =
                evaluation.detectionEvaluations.output_AnomalyDetectorResult_0;
            anomaliesDetector.anomalies = [anomaly];
            setAlertEvaluation(evaluation);
        }
    }, [evaluation]);

    if (!!anomalyId && !isValidNumberId(anomalyId)) {
        // Invalid id
        notify(
            NotificationTypeV1.Error,
            t("message.invalid-id", {
                entity: t("label.anomaly"),
                id: anomalyId,
            })
        );

        setUiAnomaly(null);
    }

    const fetchAlertEvaluation = (): void => {
        const start = searchParams.get(TimeRangeQueryStringKey.START_TIME);
        const end = searchParams.get(TimeRangeQueryStringKey.END_TIME);

        if (!uiAnomaly || !uiAnomaly.alertId || !start || !end) {
            setAlertEvaluation(null);

            return;
        }
        getEvaluation(
            createAlertEvaluation(uiAnomaly.alertId, Number(start), Number(end))
        );
    };

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

    useEffect(() => {
        if (getAnomalyRequestStatus === ActionStatus.Error) {
            isEmpty(anomalyRequestErrors)
                ? notify(
                      NotificationTypeV1.Error,
                      t("message.error-while-fetching", {
                          entity: t("label.anomaly"),
                      })
                  )
                : anomalyRequestErrors.map((msg) =>
                      notify(NotificationTypeV1.Error, msg)
                  );
        }
    }, [getAnomalyRequestStatus, anomalyRequestErrors]);

    return (
        <PageV1>
            <PageHeader showTimeRange title={pageTitle} />
            <PageContentsGridV1>
                {/* Anomaly Summary */}
                <Grid item xs={12}>
                    {getAnomalyRequestStatus === ActionStatus.Working && (
                        <Paper elevation={0}>
                            <Card variant="outlined">
                                <CardContent>
                                    <AppLoadingIndicatorV1 />
                                </CardContent>
                            </Card>
                        </Paper>
                    )}
                    {getAnomalyRequestStatus !== ActionStatus.Working &&
                        getAnomalyRequestStatus !== ActionStatus.Error && (
                            <Grid
                                container
                                alignItems="stretch"
                                justifyContent="space-between"
                            >
                                <Grid item lg={9} md={8} sm={12} xs={12}>
                                    <Paper
                                        className={style.fullHeight}
                                        elevation={0}
                                    >
                                        <AnomalySummaryCard
                                            className={style.fullHeight}
                                            uiAnomaly={uiAnomaly}
                                        />
                                    </Paper>
                                </Grid>
                                <Grid item lg={3} md={4} sm={12} xs={12}>
                                    <Paper
                                        className={style.fullHeight}
                                        elevation={0}
                                    >
                                        {anomaly && (
                                            <AnomalyFeedback
                                                anomalyFeedback={
                                                    anomaly.feedback || {
                                                        ...DEFAULT_FEEDBACK,
                                                    }
                                                }
                                                anomalyId={anomaly.id}
                                                className={style.fullHeight}
                                            />
                                        )}
                                    </Paper>
                                </Grid>
                            </Grid>
                        )}
                    {getAnomalyRequestStatus === ActionStatus.Error && (
                        <Card variant="outlined">
                            <CardContent>
                                <NoDataIndicator />
                            </CardContent>
                        </Card>
                    )}
                </Grid>

                {/* Trending */}
                <Grid item xs={12}>
                    <Paper elevation={0}>
                        {getEvaluationRequestStatus === ActionStatus.Error && (
                            <Card variant="outlined">
                                <CardContent>
                                    <Box pb={20} pt={20}>
                                        <NoDataIndicator />
                                    </Box>
                                </CardContent>
                            </Card>
                        )}
                        {getEvaluationRequestStatus === ActionStatus.Done && (
                            <AlertEvaluationTimeSeriesCard
                                alertEvaluation={alertEvaluation}
                                alertEvaluationTimeSeriesHeight={500}
                                maximizedTitle={uiAnomaly ? uiAnomaly.name : ""}
                                onRefresh={fetchAlertEvaluation}
                            />
                        )}
                    </Paper>
                </Grid>

                {/* Trending */}
                <Grid item xs={12}>
                    {anomaly && (
                        <AnalysisTabs
                            anomaly={anomaly}
                            anomalyId={toNumber(anomalyId)}
                        />
                    )}
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
