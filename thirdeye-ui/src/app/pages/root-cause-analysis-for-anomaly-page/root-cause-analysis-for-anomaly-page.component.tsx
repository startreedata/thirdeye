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
import { Grid } from "@material-ui/core";
import { clone } from "lodash";
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useOutletContext, useParams } from "react-router-dom";
import { AnomalySummaryCard } from "../../components/entity-cards/root-cause-analysis/anomaly-summary-card/anomaly-summary-card.component";
import { NoDataIndicator } from "../../components/no-data-indicator/no-data-indicator.component";
import { AnalysisTabs } from "../../components/rca/analysis-tabs/analysis-tabs.component";
import { AnomalyFilterOption } from "../../components/rca/anomaly-breakdown-comparison-heatmap/anomaly-breakdown-comparison-heatmap.interfaces";
import { AnomalyTimeSeriesCard } from "../../components/rca/anomaly-time-series-card/anomaly-time-series-card.component";
import {
    NotificationTypeV1,
    PageContentsCardV1,
    PageContentsGridV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { Event } from "../../rest/dto/event.interfaces";
import { Investigation, SavedStateKeys } from "../../rest/dto/rca.interfaces";
import { UiAnomaly } from "../../rest/dto/ui-anomaly.interfaces";
import { getUiAnomaly } from "../../utils/anomalies/anomalies.util";
import { getFromSavedInvestigationOrDefault } from "../../utils/investigation/investigation.util";
import { notifyIfErrors } from "../../utils/notifications/notifications.util";
import {
    isValidNumberId,
    serializeKeyValuePair,
} from "../../utils/params/params.util";
import { InvestigationContext } from "../root-cause-analysis-investigation-state-tracker/investigation-state-tracker.interfaces";
import { RootCauseAnalysisForAnomalyPageParams } from "./root-cause-analysis-for-anomaly-page.interfaces";
import { useRootCauseAnalysisForAnomalyPageStyles } from "./root-cause-analysis-for-anomaly-page.style";

export const RootCauseAnalysisForAnomalyPage: FunctionComponent = () => {
    const {
        investigation,
        investigationHasChanged,
        getEnumerationItemRequest,
        enumerationItem,
        anomaly,
        getAnomalyRequestStatus,
        anomalyRequestErrors,
    } = useOutletContext<InvestigationContext>();
    const [uiAnomaly, setUiAnomaly] = useState<UiAnomaly | null>(null);
    const [chartTimeSeriesFilterSet, setChartTimeSeriesFilterSet] = useState<
        AnomalyFilterOption[][]
    >(
        getFromSavedInvestigationOrDefault<AnomalyFilterOption[][]>(
            investigation,
            SavedStateKeys.CHART_FILTER_SET,
            []
        )
    );
    const [selectedEvents, setSelectedEvents] = useState<Event[]>(
        getFromSavedInvestigationOrDefault<Event[]>(
            investigation,
            SavedStateKeys.EVENT_SET,
            []
        )
    );

    const { notify } = useNotificationProviderV1();
    const { id: anomalyId } =
        useParams<RootCauseAnalysisForAnomalyPageParams>();
    const { t } = useTranslation();
    const style = useRootCauseAnalysisForAnomalyPageStyles();
    const parsedAnomalyId = useMemo(() => {
        return Number(anomalyId);
    }, [anomalyId]);

    useEffect(() => {
        if (investigation) {
            const copied: Investigation = { ...investigation };
            copied.uiMetadata[SavedStateKeys.CHART_FILTER_SET] =
                chartTimeSeriesFilterSet;
            investigationHasChanged(copied);
        }
    }, [chartTimeSeriesFilterSet]);

    // save selected events to investigation
    useEffect(() => {
        if (investigation) {
            const copied: Investigation = { ...investigation };
            copied.uiMetadata[SavedStateKeys.EVENT_SET] = clone(selectedEvents);
            investigationHasChanged(copied);
        }
    }, [selectedEvents]);

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
        notifyIfErrors(
            getAnomalyRequestStatus,
            anomalyRequestErrors,
            notify,
            t("message.error-while-fetching", {
                entity: t("label.anomaly"),
            })
        );
    }, [getAnomalyRequestStatus, anomalyRequestErrors]);

    const handleAddFilterSetClick = (filters: AnomalyFilterOption[]): void => {
        const serializedFilters = serializeKeyValuePair(filters);
        const existingIndex = chartTimeSeriesFilterSet.findIndex(
            (existingFilters) =>
                serializeKeyValuePair(existingFilters) === serializedFilters
        );
        if (existingIndex === -1) {
            setChartTimeSeriesFilterSet((original) => [
                ...original,
                [...filters], // Make a copy of filters so changes to the reference one doesn't affect it
            ]);
        } else {
            handleRemoveBtnClick(existingIndex);
        }
    };

    const handleRemoveBtnClick = (idx: number): void => {
        setChartTimeSeriesFilterSet((original) =>
            original.filter((_, index) => index !== idx)
        );
    };

    const handleEventSelectionChange = (selectedEvents: Event[]): void => {
        setSelectedEvents([...selectedEvents]);
    };

    return (
        <PageContentsGridV1>
            {/* Anomaly Summary */}
            <Grid item xs={12}>
                <Grid
                    container
                    alignItems="stretch"
                    justifyContent="space-between"
                >
                    <Grid item lg={12} md={12} sm={12} xs={12}>
                        <AnomalySummaryCard
                            className={style.fullHeight}
                            isLoading={
                                getAnomalyRequestStatus === ActionStatus.Working
                            }
                            uiAnomaly={uiAnomaly}
                        />
                    </Grid>
                </Grid>
                {getAnomalyRequestStatus === ActionStatus.Error && (
                    <Grid item xs={12}>
                        <PageContentsCardV1>
                            <NoDataIndicator />
                        </PageContentsCardV1>
                    </Grid>
                )}
            </Grid>

            {/* Trending */}
            <Grid item xs={12}>
                <AnomalyTimeSeriesCard
                    anomaly={anomaly}
                    // Selected events should be shown on the graph
                    enumerationItem={enumerationItem}
                    events={selectedEvents}
                    getEnumerationItemRequest={getEnumerationItemRequest}
                    isLoading={getAnomalyRequestStatus === ActionStatus.Working}
                    timeSeriesFiltersSet={chartTimeSeriesFilterSet}
                    onEventSelectionChange={handleEventSelectionChange}
                    onRemoveBtnClick={handleRemoveBtnClick}
                />
            </Grid>

            {/* Dimension Related */}
            <Grid item xs={12}>
                <AnalysisTabs
                    anomaly={anomaly}
                    anomalyId={parsedAnomalyId}
                    chartTimeSeriesFilterSet={chartTimeSeriesFilterSet}
                    isLoading={getAnomalyRequestStatus === ActionStatus.Working}
                    selectedEvents={selectedEvents}
                    onAddFilterSetClick={handleAddFilterSetClick}
                    onEventSelectionChange={handleEventSelectionChange}
                />
            </Grid>
        </PageContentsGridV1>
    );
};
