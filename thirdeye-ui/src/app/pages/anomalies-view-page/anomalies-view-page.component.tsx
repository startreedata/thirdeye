import { Box, Card, CardContent, Grid, Paper } from "@material-ui/core";
import { isEmpty, toNumber } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import { AnomalyFeedback } from "../../components/anomlay-feedback/anomaly-feedback.component";
import { useDialog } from "../../components/dialogs/dialog-provider/dialog-provider.component";
import { DialogType } from "../../components/dialogs/dialog-provider/dialog-provider.interfaces";
import { AnomalyCard } from "../../components/entity-cards/anomaly-card/anomaly-card.component";
import { HelpIconWithTooltip } from "../../components/help-icon-with-tooltip/help-icon-with-tooltip.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { AnalysisTabs } from "../../components/rca/analysis-tabs/analysis-tabs.component";
import { TimeRangeQueryStringKey } from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import { AlertEvaluationTimeSeriesCard } from "../../components/visualizations/alert-evaluation-time-series-card/alert-evaluation-time-series-card.component";
import {
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetEvaluation } from "../../rest/alerts/alerts.actions";
import { deleteAnomaly } from "../../rest/anomalies/anomalies.rest";
import { useGetAnomaly } from "../../rest/anomalies/anomaly.actions";
import { AlertEvaluation } from "../../rest/dto/alert.interfaces";
import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";
import { DEFAULT_FEEDBACK } from "../../utils/alerts/alerts.util";
import {
    createAlertEvaluation,
    getUiAnomaly,
} from "../../utils/anomalies/anomalies.util";
import { isValidNumberId } from "../../utils/params/params.util";
import { getAnomaliesAllPath } from "../../utils/routes/routes.util";
import { AnomaliesViewPageParams } from "./anomalies-view-page.interfaces";
import { useAnomaliesViewPageStyles } from "./anomalies-view-page.styles";

export const AnomaliesViewPage: FunctionComponent = () => {
    const {
        evaluation,
        getEvaluation,
        errorMessages,
        status: getEvaluationRequestStatus,
    } = useGetEvaluation();
    const {
        anomaly,
        getAnomaly,
        status: anomalyRequestStatus,
        errorMessages: anomalyRequestErrors,
    } = useGetAnomaly();
    const [uiAnomaly, setUiAnomaly] = useState<UiAnomaly | null>(null);
    const [alertEvaluation, setAlertEvaluation] =
        useState<AlertEvaluation | null>(null);
    const [searchParams] = useSearchParams();
    const { showDialog } = useDialog();
    const { id: anomalyId } = useParams<AnomaliesViewPageParams>();
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();
    const style = useAnomaliesViewPageStyles();

    useEffect(() => {
        // Time range refreshed, fetch anomaly
        anomalyId &&
            isValidNumberId(anomalyId) &&
            getAnomaly(toNumber(anomalyId));
    }, [anomalyId]);

    useEffect(() => {
        !!anomaly && setUiAnomaly(getUiAnomaly(anomaly));
    }, [anomaly]);

    useEffect(() => {
        if (!evaluation || !anomaly) {
            return;
        }
        // Only filter for the current anomaly
        const anomalyDetectionResults =
            evaluation.detectionEvaluations.output_AnomalyDetectorResult_0;
        anomalyDetectionResults.anomalies = [anomaly];
        setAlertEvaluation(evaluation);
    }, [evaluation, anomaly]);

    useEffect(() => {
        // Fetched alert or time range changed, fetch alert evaluation
        fetchAlertEvaluation();
    }, [anomaly, searchParams]);

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

    if (anomalyId && !isValidNumberId(anomalyId)) {
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

        if (!anomaly || !anomaly.alert || !start || !end) {
            setAlertEvaluation(null);

            return;
        }
        getEvaluation(
            createAlertEvaluation(anomaly.alert.id, Number(start), Number(end))
        );
    };

    const handleAnomalyDelete = (uiAnomaly: UiAnomaly): void => {
        showDialog({
            type: DialogType.ALERT,
            text: t("message.delete-confirmation", { name: uiAnomaly.name }),
            okButtonLabel: t("label.delete"),
            onOk: () => handleAnomalyDeleteOk(uiAnomaly),
        });
    };

    const handleAnomalyDeleteOk = (uiAnomaly: UiAnomaly): void => {
        deleteAnomaly(uiAnomaly.id).then(() => {
            notify(
                NotificationTypeV1.Success,
                t("message.delete-success", { entity: t("label.anomaly") })
            );

            // Redirect to anomalies all path
            navigate(getAnomaliesAllPath());
        });
    };

    useEffect(() => {
        if (anomalyRequestStatus === ActionStatus.Error) {
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
    }, [anomalyRequestStatus, anomalyRequestErrors]);

    return (
        <PageV1>
            <PageHeader
                showCreateButton
                showTimeRange
                title={uiAnomaly ? uiAnomaly.name : ""}
            >
                <HelpIconWithTooltip
                    href="https://dev.startree.ai/docs/thirdeye/how-tos/perform-root-cause-analysis"
                    tooltipTitle={
                        t(
                            "label.how-to-perform-root-cause-analysis-doc"
                        ) as string
                    }
                />
            </PageHeader>
            <PageContentsGridV1>
                {/* Anomaly */}
                <Grid
                    container
                    item
                    alignItems="stretch"
                    justifyContent="space-between"
                    xs={12}
                >
                    <Grid item lg={9} md={8} sm={12} xs={12}>
                        <Paper className={style.fullHeight} elevation={0}>
                            <AnomalyCard
                                uiAnomaly={uiAnomaly}
                                onDelete={handleAnomalyDelete}
                            />
                        </Paper>
                    </Grid>
                    <Grid item lg={3} md={4} sm={12} xs={12}>
                        <Paper className={style.fullHeight} elevation={0}>
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

                {/* Alert evaluation time series */}
                <Grid item xs={12}>
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
                </Grid>

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
