import { Card, CardContent, Grid } from "@material-ui/core";
import { isEmpty, toNumber } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { useParams } from "react-router-dom";
import { AnomalyFilterOption } from "../../components/anomaly-breakdown-comparison-heatmap/anomaly-breakdown-comparison-heatmap.interfaces";
import { AnomalyFeedback } from "../../components/anomlay-feedback/anomaly-feedback.component";
import { AnomalySummaryCard } from "../../components/entity-cards/root-cause-analysis/anomaly-summary-card/anomaly-summary-card.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { PageHeader } from "../../components/page-header/page-header.component";
import { AnalysisTabs } from "../../components/rca/analysis-tabs/analysis-tabs.component";
import { AnomalyTimeSeriesCard } from "../../components/rca/anomaly-time-series-card/anomaly-time-series-card.component";
import {
    AppLoadingIndicatorV1,
    HelpLinkIconV1,
    NotificationTypeV1,
    PageContentsGridV1,
    PageV1,
    TooltipV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { useGetAnomaly } from "../../rest/anomalies/anomaly.actions";
import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";
import { DEFAULT_FEEDBACK } from "../../utils/alerts/alerts.util";
import { getUiAnomaly } from "../../utils/anomalies/anomalies.util";
import {
    isValidNumberId,
    serializeKeyValuePair,
} from "../../utils/params/params.util";
import { RootCauseAnalysisForAnomalyPageParams } from "./root-cause-analysis-for-anomaly-page.interfaces";
import { useRootCauseAnalysisForAnomalyPageStyles } from "./root-cause-analysis-for-anomaly-page.style";

export const RootCauseAnalysisForAnomalyPage: FunctionComponent = () => {
    const {
        anomaly,
        getAnomaly,
        status: getAnomalyRequestStatus,
        errorMessages: anomalyRequestErrors,
    } = useGetAnomaly();
    const [uiAnomaly, setUiAnomaly] = useState<UiAnomaly | null>(null);
    const [chartTimeSeriesFilterSet, setChartTimeSeriesFilterSet] = useState<
        AnomalyFilterOption[][]
    >([]);
    const { notify } = useNotificationProviderV1();
    const { id: anomalyId } =
        useParams<RootCauseAnalysisForAnomalyPageParams>();
    const { t } = useTranslation();
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

    const handleAddFilterSetClick = (filters: AnomalyFilterOption[]): void => {
        const serializedFilters = serializeKeyValuePair(filters);
        const existingIndex = chartTimeSeriesFilterSet.findIndex(
            (existingFilters) =>
                serializeKeyValuePair(existingFilters) === serializedFilters
        );
        if (existingIndex === -1) {
            setChartTimeSeriesFilterSet((original) => [...original, filters]);
        } else {
            handleRemoveBtnClick(existingIndex);
        }
    };

    const handleRemoveBtnClick = (idx: number): void => {
        setChartTimeSeriesFilterSet((original) =>
            original.filter((_, index) => index !== idx)
        );
    };

    return (
        <PageV1>
            <PageHeader showTimeRange title={pageTitle}>
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
                            href="https://dev.startree.ai/docs/thirdeye/how-tos/perform-root-cause-analysis"
                        />
                    </span>
                </TooltipV1>
            </PageHeader>
            <PageContentsGridV1>
                {/* Anomaly Summary */}
                <Grid item xs={12}>
                    {getAnomalyRequestStatus === ActionStatus.Working && (
                        <Card variant="outlined">
                            <CardContent>
                                <AppLoadingIndicatorV1 />
                            </CardContent>
                        </Card>
                    )}
                    {getAnomalyRequestStatus !== ActionStatus.Working &&
                        getAnomalyRequestStatus !== ActionStatus.Error && (
                            <Grid
                                container
                                alignItems="stretch"
                                justifyContent="space-between"
                            >
                                <Grid item lg={9} md={8} sm={12} xs={12}>
                                    <AnomalySummaryCard
                                        className={style.fullHeight}
                                        uiAnomaly={uiAnomaly}
                                    />
                                </Grid>
                                <Grid item lg={3} md={4} sm={12} xs={12}>
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
                    {anomaly && (
                        <AnomalyTimeSeriesCard
                            anomaly={anomaly}
                            timeSeriesFiltersSet={chartTimeSeriesFilterSet}
                            onRemoveBtnClick={handleRemoveBtnClick}
                        />
                    )}
                </Grid>

                {/* Dimension Related */}
                <Grid item xs={12}>
                    {anomaly && (
                        <AnalysisTabs
                            anomaly={anomaly}
                            anomalyId={toNumber(anomalyId)}
                            chartTimeSeriesFilterSet={chartTimeSeriesFilterSet}
                            onAddFilterSetClick={handleAddFilterSetClick}
                        />
                    )}
                </Grid>
            </PageContentsGridV1>
        </PageV1>
    );
};
