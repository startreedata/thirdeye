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
import { Box, Grid, Link } from "@material-ui/core";
import { isEmpty, toNumber } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import { AnomalyFeedback } from "../../components/anomlay-feedback/anomaly-feedback.component";
import { AnomalyCard } from "../../components/entity-cards/anomaly-card/anomaly-card.component";
import { InvestigationsList } from "../../components/investigations-list/investigations-list.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { TimeRangeQueryStringKey } from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import { AlertEvaluationTimeSeriesCard } from "../../components/visualizations/alert-evaluation-time-series-card/alert-evaluation-time-series-card.component";
import {
    HelpLinkIconV1,
    NotificationTypeV1,
    PageContentsCardV1,
    PageContentsGridV1,
    PageV1,
    TooltipV1,
    useDialogProviderV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { DialogType } from "../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetEvaluation } from "../../rest/alerts/alerts.actions";
import { deleteAnomaly } from "../../rest/anomalies/anomalies.rest";
import { useGetAnomaly } from "../../rest/anomalies/anomaly.actions";
import { AlertEvaluation } from "../../rest/dto/alert.interfaces";
import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";
import { useGetInvestigations } from "../../rest/rca/rca.actions";
import { DEFAULT_FEEDBACK } from "../../utils/alerts/alerts.util";
import {
    createAlertEvaluation,
    getUiAnomaly,
} from "../../utils/anomalies/anomalies.util";
import { THIRDEYE_DOC_LINK } from "../../utils/constants/constants.util";
import { isValidNumberId } from "../../utils/params/params.util";
import {
    getAlertsViewPath,
    getAnomaliesAllPath,
} from "../../utils/routes/routes.util";
import { AnomaliesViewPageParams } from "./anomalies-view-page.interfaces";
import { useAnomaliesViewPageStyles } from "./anomalies-view-page.styles";

export const AnomaliesViewPage: FunctionComponent = () => {
    const {
        investigations,
        getInvestigations,
        status: getInvestigationsRequestStatus,
    } = useGetInvestigations();
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
    const { showDialog } = useDialogProviderV1();
    const { id: anomalyId } = useParams<AnomaliesViewPageParams>();
    const navigate = useNavigate();
    const { t } = useTranslation();
    const { notify } = useNotificationProviderV1();
    const style = useAnomaliesViewPageStyles();

    useEffect(() => {
        anomalyId && getInvestigations(Number(anomalyId));
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
            contents: t("message.delete-confirmation", {
                name: uiAnomaly.name,
            }),
            okButtonText: t("label.delete"),
            cancelButtonText: t("label.cancel"),
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
            <PageHeader title="">
                {anomaly && uiAnomaly && (
                    <>
                        <Link href={getAlertsViewPath(anomaly.alert.id)}>
                            {anomaly.alert.name}
                        </Link>
                        : {uiAnomaly.name}
                    </>
                )}
                <TooltipV1
                    placement="top"
                    title={
                        t(
                            "label.how-to-perform-root-cause-analysis-doc"
                        ) as string
                    }
                >
                    <span>
                        <HelpLinkIconV1
                            displayInline
                            enablePadding
                            externalLink
                            href={`${THIRDEYE_DOC_LINK}/how-tos/perform-root-cause-analysis`}
                        />
                    </span>
                </TooltipV1>
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
                        <AnomalyCard
                            className={style.fullHeight}
                            isLoading={
                                anomalyRequestStatus === ActionStatus.Working
                            }
                            uiAnomaly={uiAnomaly}
                            onDelete={handleAnomalyDelete}
                        />
                    </Grid>
                    <Grid item lg={3} md={4} sm={12} xs={12}>
                        <AnomalyFeedback
                            anomalyFeedback={
                                (anomaly && anomaly.feedback) || {
                                    ...DEFAULT_FEEDBACK,
                                }
                            }
                            anomalyId={Number(anomalyId)}
                            className={style.fullHeight}
                            isLoading={
                                anomalyRequestStatus === ActionStatus.Working
                            }
                        />
                    </Grid>
                </Grid>

                {/* Alert evaluation time series */}
                <Grid item xs={12}>
                    {getEvaluationRequestStatus === ActionStatus.Error && (
                        <PageContentsCardV1>
                            <Box pb={20} pt={20}>
                                <NoDataIndicator />
                            </Box>
                        </PageContentsCardV1>
                    )}
                    {getEvaluationRequestStatus !== ActionStatus.Error && (
                        <AlertEvaluationTimeSeriesCard
                            alertEvaluation={alertEvaluation}
                            alertEvaluationTimeSeriesHeight={500}
                            isLoading={
                                getEvaluationRequestStatus ===
                                ActionStatus.Working
                            }
                            maximizedTitle={uiAnomaly ? uiAnomaly.name : ""}
                            onRefresh={fetchAlertEvaluation}
                        />
                    )}
                </Grid>

                {/* Existing investigations */}
                <Grid item xs={12}>
                    <InvestigationsList
                        getInvestigationsRequestStatus={
                            getInvestigationsRequestStatus
                        }
                        investigations={investigations}
                    />
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
