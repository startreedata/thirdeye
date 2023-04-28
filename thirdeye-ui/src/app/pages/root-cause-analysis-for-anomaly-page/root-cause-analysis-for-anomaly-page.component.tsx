/*
 * Copyright 2023 StarTree Inc
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
import { AnomalyCard } from "../../components/entity-cards/anomaly-card/anomaly-card.component";
import { LoadingErrorStateSwitch } from "../../components/page-states/loading-error-state-switch/loading-error-state-switch.component";
import { AnalysisTabs } from "../../components/rca/analysis-tabs/analysis-tabs.component";
import { AnomalyFilterOption } from "../../components/rca/anomaly-breakdown-comparison-heatmap/anomaly-breakdown-comparison-heatmap.interfaces";
import { AnomalyTimeSeriesCard } from "../../components/rca/anomaly-time-series-card/anomaly-time-series-card.component";
import {
    NotificationTypeV1,
    PageContentsGridV1,
    useNotificationProviderV1,
} from "../../platform/components";
import { ActionStatus } from "../../rest/actions.interfaces";
import { AlertEvaluation } from "../../rest/dto/alert.interfaces";
import { Event } from "../../rest/dto/event.interfaces";
import { Investigation, SavedStateKeys } from "../../rest/dto/rca.interfaces";
import {
    determineTimezoneFromAlertInEvaluation,
    shouldHideTimeInDatetimeFormat,
} from "../../utils/alerts/alerts.util";
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
    const { notify } = useNotificationProviderV1();
    const { t } = useTranslation();
    const style = useRootCauseAnalysisForAnomalyPageStyles();
    const { id: anomalyId } =
        useParams<RootCauseAnalysisForAnomalyPageParams>();
    const {
        investigation,
        investigationHasChanged,
        getEnumerationItemRequest,
        enumerationItem,
        anomaly,
        getAnomalyRequestStatus,
        anomalyRequestErrors,
    } = useOutletContext<InvestigationContext>();

    const [timezone, setTimezone] = useState<string | undefined>("UTC");
    const [hideTime, setHideTime] = useState<boolean>(false);
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

    if (!!anomalyId && !isValidNumberId(anomalyId)) {
        // Invalid id
        notify(
            NotificationTypeV1.Error,
            t("message.invalid-id", {
                entity: t("label.anomaly"),
                id: anomalyId,
            })
        );
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
                    <Grid item xs={12}>
                        <LoadingErrorStateSwitch
                            wrapInCard
                            isError={
                                getAnomalyRequestStatus === ActionStatus.Error
                            }
                            isLoading={
                                getAnomalyRequestStatus === ActionStatus.Working
                            }
                        >
                            <AnomalyCard
                                anomaly={anomaly}
                                className={style.fullHeight}
                                hideTime={hideTime}
                                isLoading={false}
                                timezone={timezone}
                            />
                        </LoadingErrorStateSwitch>
                    </Grid>
                </Grid>
            </Grid>

            {/* Trending */}
            <Grid item xs={12}>
                <AnomalyTimeSeriesCard
                    anomaly={anomaly}
                    enumerationItem={enumerationItem}
                    // Selected events should be shown on the graph
                    events={selectedEvents}
                    getEnumerationItemRequest={getEnumerationItemRequest}
                    isLoading={
                        getAnomalyRequestStatus === ActionStatus.Working ||
                        getAnomalyRequestStatus === ActionStatus.Initial
                    }
                    timeSeriesFiltersSet={chartTimeSeriesFilterSet}
                    onAlertEvaluationDidFetch={(
                        evaluation: AlertEvaluation
                    ) => {
                        setTimezone(
                            determineTimezoneFromAlertInEvaluation(
                                evaluation?.alert.template
                            )
                        );
                        setHideTime(
                            shouldHideTimeInDatetimeFormat(
                                evaluation?.alert.template
                            )
                        );
                    }}
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
                    hideTime={hideTime}
                    isLoading={getAnomalyRequestStatus === ActionStatus.Working}
                    selectedEvents={selectedEvents}
                    timezone={timezone}
                    onAddFilterSetClick={handleAddFilterSetClick}
                    onEventSelectionChange={handleEventSelectionChange}
                />
            </Grid>
        </PageContentsGridV1>
    );
};
