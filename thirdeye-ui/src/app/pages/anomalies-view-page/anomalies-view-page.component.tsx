/*
 * Copyright 2022 StarTree Inc
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
import { Box, Button, Grid, Link } from "@material-ui/core";
import { toNumber } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams, useSearchParams } from "react-router-dom";
import { AnomalyCard } from "../../components/entity-cards/anomaly-card/anomaly-card.component";
import { IframeVideoPlayerContainer } from "../../components/iframe-video-player-container/iframe-video-player-container.component";
import { InvestigationsList } from "../../components/investigations-list/investigations-list.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { TimeRangeQueryStringKey } from "../../components/time-range/time-range-provider/time-range-provider.interfaces";
import { AlertEvaluationTimeSeriesCard } from "../../components/visualizations/alert-evaluation-time-series-card/alert-evaluation-time-series-card.component";
import { ViewAnomalyHeader } from "../../components/visualizations/alert-evaluation-time-series-card/headers/view-anomaly-header.component";
import {
    HelpLinkIconV1,
    NotificationTypeV1,
    PageContentsCardV1,
    PageContentsGridV1,
    PageHeaderActionsV1,
    PageHeaderTextV1,
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
import { Anomaly } from "../../rest/dto/anomaly.interfaces";
import { DetectionEvaluation } from "../../rest/dto/detection.interfaces";
import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";
import { useGetEnumerationItem } from "../../rest/enumeration-items/enumeration-items.actions";
import { useGetInvestigations } from "../../rest/rca/rca.actions";
import {
    determineTimezoneFromAlertInEvaluation,
    extractDetectionEvaluation,
} from "../../utils/alerts/alerts.util";
import {
    createAlertEvaluation,
    getUiAnomaly,
} from "../../utils/anomalies/anomalies.util";
import { THIRDEYE_DOC_LINK } from "../../utils/constants/constants.util";
import { generateNameForEnumerationItem } from "../../utils/enumeration-items/enumeration-items.util";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import {
    isValidNumberId,
    QUERY_PARAM_KEY_FOR_EXPANDED,
} from "../../utils/params/params.util";
import {
    getAlertsAlertPath,
    getAnomaliesAllPath,
    getRootCauseAnalysisForAnomalyInvestigatePath,
} from "../../utils/routes/routes.util";
import { AnomaliesViewPageParams } from "./anomalies-view-page.interfaces";
import { useAnomaliesViewPageStyles } from "./anomalies-view-page.styles";

export const AnomaliesViewPage: FunctionComponent = () => {
    const {
        enumerationItem,
        getEnumerationItem,
        status: getEnumerationItemRequest,
    } = useGetEnumerationItem();
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
    const [detectionEvaluation, setDetectionEvaluation] =
        useState<DetectionEvaluation | null>(null);
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
        /**
         * If anomaly has an enumeration id, fetch the enumeration item so
         * that we can use the correct detection evaluation
         */
        !!anomaly &&
            anomaly.enumerationItem &&
            getEnumerationItem(anomaly.enumerationItem.id);
    }, [anomaly]);

    useEffect(() => {
        if (!evaluation || !anomaly) {
            return;
        }

        const detectionEvalForAnomaly =
            extractDetectionEvaluation(evaluation)[0];

        // Only filter for the current anomaly
        detectionEvalForAnomaly.anomalies = [anomaly];
        setDetectionEvaluation(detectionEvalForAnomaly);
    }, [evaluation, anomaly]);

    useEffect(() => {
        // Fetched alert or time range changed, fetch alert evaluation
        fetchAlertEvaluation();
    }, [anomaly, searchParams]);

    useEffect(() => {
        notifyIfErrors(
            getEvaluationRequestStatus,
            errorMessages,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.chart-data"),
            })
        );
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
            setDetectionEvaluation(null);

            return;
        }
        getEvaluation(
            createAlertEvaluation(anomaly.alert.id, Number(start), Number(end)),
            undefined,
            anomaly.enumerationItem
        );
    };

    const handleAnomalyDelete = (): void => {
        if (!uiAnomaly) {
            return;
        }
        showDialog({
            type: DialogType.ALERT,
            contents: t("message.delete-confirmation", {
                name: uiAnomaly.name,
            }),
            okButtonText: t("label.confirm"),
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
        notifyIfErrors(
            anomalyRequestStatus,
            anomalyRequestErrors,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.anomaly"),
            })
        );
    }, [anomalyRequestStatus, anomalyRequestErrors]);

    const enumerationItemSearchParams =
        anomaly && anomaly.enumerationItem && enumerationItem
            ? new URLSearchParams([
                  [
                      QUERY_PARAM_KEY_FOR_EXPANDED,
                      generateNameForEnumerationItem(enumerationItem),
                  ],
              ])
            : undefined;

    /**
     * Chart data will have issues if the evaluation request errors or
     * anomaly belongs to an enumeration item and its request errors
     */
    const chartDataHasIssues =
        getEvaluationRequestStatus === ActionStatus.Error ||
        (anomaly &&
            anomaly.enumerationItem &&
            getEnumerationItemRequest === ActionStatus.Error);

    const getSubtitle = (): string | undefined => {
        if (anomaly && anomaly.enumerationItem && enumerationItem) {
            return generateNameForEnumerationItem(enumerationItem);
        }

        if (uiAnomaly?.isIgnored) {
            return t(
                "message.this-anomaly-was-ignored-due-to-the-system-sensibility"
            );
        }

        return undefined;
    };

    const getAnomalyName = ({ name, isIgnored }: UiAnomaly): string =>
        `${name}${isIgnored ? `(${t("label.ignored")})` : ""}`;

    const onHowInvestigateClick = (): void => {
        showDialog({
            type: DialogType.CUSTOM,
            width: "md",
            contents: (
                <IframeVideoPlayerContainer>
                    <iframe
                        allowFullScreen
                        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture"
                        src={t("url.how-to-use-te-video")}
                    />
                </IframeVideoPlayerContainer>
            ),
            hideOkButton: true,
            cancelButtonText: t("label.close"),
        });
    };

    return (
        <PageV1>
            <PageHeader
                breadcrumbs={[
                    {
                        link: getAnomaliesAllPath(),
                        label: t("label.anomalies"),
                    },
                    {
                        label: anomalyId,
                    },
                ]}
                customActions={
                    <PageHeaderActionsV1>
                        <Button
                            color="secondary"
                            variant="contained"
                            onClick={onHowInvestigateClick}
                        >
                            {t("label.how-to-investigate")}
                        </Button>
                        <Button
                            color="primary"
                            component="button"
                            href={`${getRootCauseAnalysisForAnomalyInvestigatePath(
                                Number(anomalyId)
                            )}?${searchParams.toString()}`}
                            variant="contained"
                        >
                            {t("label.investigate-entity", {
                                entity: t("label.anomaly"),
                            })}
                        </Button>
                        <Button
                            component="button"
                            variant="contained"
                            onClick={handleAnomalyDelete}
                        >
                            {t("label.delete")}
                        </Button>
                    </PageHeaderActionsV1>
                }
                subtitle={getSubtitle()}
            >
                <PageHeaderTextV1>
                    {anomaly && uiAnomaly && (
                        <>
                            <Link
                                href={getAlertsAlertPath(
                                    anomaly.alert.id,
                                    enumerationItemSearchParams
                                )}
                            >
                                {anomaly.alert.name}
                            </Link>
                            : {getAnomalyName(uiAnomaly)}
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
                </PageHeaderTextV1>
            </PageHeader>

            <PageContentsGridV1>
                {/* Anomaly */}
                <Grid item xs={12}>
                    <AnomalyCard
                        className={style.fullHeight}
                        isLoading={
                            anomalyRequestStatus === ActionStatus.Working
                        }
                        timezone={determineTimezoneFromAlertInEvaluation(
                            evaluation?.alert
                        )}
                        uiAnomaly={uiAnomaly}
                    />
                </Grid>

                {/* Alert evaluation time series */}
                <Grid item xs={12}>
                    {chartDataHasIssues && (
                        <PageContentsCardV1>
                            <Box pb={20} pt={20}>
                                <NoDataIndicator />
                            </Box>
                        </PageContentsCardV1>
                    )}
                    {!chartDataHasIssues && (
                        <AlertEvaluationTimeSeriesCard
                            disableNavigation
                            alertEvaluationTimeSeriesHeight={500}
                            anomalies={[anomaly as Anomaly]}
                            detectionEvaluation={detectionEvaluation}
                            header={
                                <ViewAnomalyHeader
                                    anomaly={anomaly}
                                    timezone={determineTimezoneFromAlertInEvaluation(
                                        evaluation?.alert
                                    )}
                                    onRefresh={fetchAlertEvaluation}
                                />
                            }
                            isLoading={
                                getEvaluationRequestStatus ===
                                ActionStatus.Working
                            }
                            timezone={determineTimezoneFromAlertInEvaluation(
                                evaluation?.alert
                            )}
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
