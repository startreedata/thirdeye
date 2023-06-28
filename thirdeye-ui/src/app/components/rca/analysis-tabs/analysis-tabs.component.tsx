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
import {
    Box,
    Card,
    CardContent,
    Divider,
    Grid,
    Tab,
    Tabs,
    Typography,
} from "@material-ui/core";
import { AxiosError } from "axios";
import React, { FunctionComponent, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useSearchParams } from "react-router-dom";
import {
    NotificationTypeV1,
    PageContentsCardV1,
    SearchInputV1,
    SkeletonV1,
    useNotificationProviderV1,
} from "../../../platform/components";
import { formatDateAndTimeV1, formatDateV1 } from "../../../platform/utils";
import { ActionStatus } from "../../../rest/actions.interfaces";
import { Anomaly } from "../../../rest/dto/anomaly.interfaces";
import { EditableEvent, Event } from "../../../rest/dto/event.interfaces";
import { createEvent } from "../../../rest/event/events.rest";
import {
    baselineOffsetToMilliseconds,
    comparisonOffsetReadableValue,
} from "../../../utils/anomaly-breakdown/anomaly-breakdown.util";
import { createEmptyEvent } from "../../../utils/events/events.util";
import { notifyIfErrors } from "../../../utils/notifications/notifications.util";
import { getErrorMessages } from "../../../utils/rest/rest.util";
import { EventsWizardModal } from "../../events-wizard-modal/event-wizard-modal.component";
import { AnomalyBreakdownComparisonHeatmap } from "../anomaly-breakdown-comparison-heatmap/anomaly-breakdown-comparison-heatmap.component";
import { AnomalyDimensionAnalysis } from "../anomaly-dimension-analysis/anomaly-dimension-analysis.component";
import { EventsTab } from "../events-tab/event-tab.component";
import { AnalysisTabsProps } from "./analysis-tabs.interfaces";
import { BaselineOffsetSelection } from "./baseline-offset-selection/baseline-offset-selection.component";

const ANALYSIS_TAB_IDX_KEY = "analysisTab";
const ANALYSIS_TAB_OFFSET = "baselineWeekOffset";

export const AnalysisTabs: FunctionComponent<AnalysisTabsProps> = ({
    anomalyId,
    anomaly,
    onAddFilterSetClick,
    chartTimeSeriesFilterSet,
    selectedEvents,
    onEventSelectionChange,
    isLoading,
    timezone,
    hideTime,
}) => {
    const { notify } = useNotificationProviderV1();
    const [searchParams, setSearchParams] = useSearchParams();
    const { t } = useTranslation();
    const [selectedTabIndex, setSelectedTabIndex] = useState(() => {
        if (searchParams.has(ANALYSIS_TAB_IDX_KEY)) {
            return Number(searchParams.get(ANALYSIS_TAB_IDX_KEY));
        }

        return 1;
    });
    const [eventsSearchValue, setEventsSearchValue] = useState("");
    const [triggerUpdateEvents, setTriggerUpdateEvents] = useState(false);

    const newEventObject = useMemo(() => {
        const event = createEmptyEvent();
        event.startTime = anomaly?.startTime as number;
        event.endTime = anomaly?.endTime as number;

        return event;
    }, [anomaly]);

    const [comparisonOffset, setComparisonOffset] = useState(() => {
        return searchParams.get(ANALYSIS_TAB_OFFSET) ?? "P1W";
    });

    const dateFormatter = useMemo(() => {
        return hideTime ? formatDateV1 : formatDateAndTimeV1;
    }, [hideTime]);

    const handleBaselineChange = (newValue: string): void => {
        setComparisonOffset(newValue);
        searchParams.set(ANALYSIS_TAB_OFFSET, newValue);
        setSearchParams(searchParams);
    };

    const handleTabIndexChange = (_event: unknown, newValue: number): void => {
        setSelectedTabIndex(newValue);
        searchParams.set(ANALYSIS_TAB_IDX_KEY, newValue.toString());
        setSearchParams(searchParams);
    };

    const handleAddEventSubmit = (newEvent: EditableEvent): void => {
        createEvent(newEvent)
            .then((event: Event): void => {
                notify(
                    NotificationTypeV1.Success,
                    t("message.create-success", {
                        entity: t("label.event"),
                    })
                );
                onEventSelectionChange([...selectedEvents, event]);
                setTriggerUpdateEvents(!triggerUpdateEvents);
            })
            .catch((error: AxiosError): void => {
                notifyIfErrors(
                    ActionStatus.Error,
                    getErrorMessages(error),
                    notify,
                    t("message.create-error", {
                        entity: t("label.event"),
                    })
                );
            });
    };

    if (isLoading) {
        return (
            <PageContentsCardV1>
                <SkeletonV1 preventDelay height={500} variant="rect" />
            </PageContentsCardV1>
        );
    }

    return (
        <Card variant="outlined">
            <CardContent>
                <Grid container>
                    <Grid item md={5} sm={6} xs={12}>
                        <Tabs
                            value={selectedTabIndex}
                            onChange={handleTabIndexChange}
                        >
                            <Tab label={t("label.heatmap")} value={1} />
                            <Tab
                                label={t("label.top-contributors")}
                                value={0}
                            />
                            <Tab label={t("label.events")} value={2} />
                        </Tabs>
                    </Grid>
                    {/* Hide baseline offset selector if events tab is selected */}
                    {selectedTabIndex !== 2 ? (
                        <Grid item md={7} sm={6} xs={12}>
                            <BaselineOffsetSelection
                                baselineOffset={comparisonOffset}
                                onBaselineOffsetChange={handleBaselineChange}
                            />
                        </Grid>
                    ) : (
                        <Grid item md={7} sm={6} xs={12}>
                            <Grid
                                container
                                alignItems="center"
                                justifyContent="flex-end"
                            >
                                <Grid item>
                                    <EventsWizardModal
                                        btnSize="large"
                                        event={newEventObject}
                                        onSubmit={handleAddEventSubmit}
                                    />
                                </Grid>

                                <Grid item>
                                    <SearchInputV1
                                        fullWidth
                                        placeholder={t("label.search-entity", {
                                            entity: t("label.event"),
                                        })}
                                        onChange={setEventsSearchValue}
                                    />
                                </Grid>
                            </Grid>
                        </Grid>
                    )}
                </Grid>
            </CardContent>

            {anomaly && selectedTabIndex !== 2 && (
                <CardContent>
                    <Grid container>
                        <Grid item xs={12}>
                            <Typography variant="h6">
                                {t("label.date-reference")}
                            </Typography>
                        </Grid>
                        <Grid item xs={6}>
                            <div>
                                <strong>
                                    &quot;{t("label.current")}&quot;
                                </strong>{" "}
                                Data Date Range
                            </div>
                            <div>
                                {dateFormatter(anomaly.startTime, timezone)}
                                <strong> to </strong>
                                {dateFormatter(anomaly.endTime, timezone)}
                            </div>
                        </Grid>
                        <Grid item xs={6}>
                            <div>
                                <strong>
                                    &quot;{t("label.baseline")}&quot;
                                </strong>
                                <span>
                                    {" "}
                                    Data Date Range (
                                    {comparisonOffsetReadableValue(
                                        comparisonOffset
                                    )}
                                    )
                                </span>
                            </div>
                            <div>
                                {dateFormatter(
                                    anomaly.startTime -
                                        baselineOffsetToMilliseconds(
                                            comparisonOffset
                                        ),
                                    timezone
                                )}
                                <strong> to </strong>
                                {dateFormatter(
                                    anomaly.endTime -
                                        baselineOffsetToMilliseconds(
                                            comparisonOffset
                                        ),
                                    timezone
                                )}
                            </div>
                        </Grid>

                        <Grid item xs={12}>
                            <Divider />
                        </Grid>
                    </Grid>
                </CardContent>
            )}
            {selectedTabIndex === 0 && (
                <Box mt={-4}>
                    <AnomalyDimensionAnalysis
                        anomaly={anomaly as Anomaly}
                        anomalyId={anomalyId}
                        chartTimeSeriesFilterSet={chartTimeSeriesFilterSet}
                        comparisonOffset={comparisonOffset}
                        hideTime={hideTime}
                        timezone={timezone}
                        onCheckClick={onAddFilterSetClick}
                    />
                </Box>
            )}
            {selectedTabIndex === 1 && (
                <Box mt={-4}>
                    <AnomalyBreakdownComparisonHeatmap
                        anomalyId={anomalyId}
                        chartTimeSeriesFilterSet={chartTimeSeriesFilterSet}
                        comparisonOffset={comparisonOffset}
                        onAddFilterSetClick={onAddFilterSetClick}
                    />
                </Box>
            )}
            {selectedTabIndex === 2 && (
                <Box mt={-2}>
                    <EventsTab
                        anomalyId={anomalyId}
                        searchValue={eventsSearchValue}
                        selectedEvents={selectedEvents}
                        timezone={timezone}
                        triggerUpdate={triggerUpdateEvents}
                        onCheckClick={onEventSelectionChange}
                    />
                </Box>
            )}
        </Card>
    );
};
