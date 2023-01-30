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
import {
    Box,
    Button,
    Card,
    CardContent,
    Divider,
    Grid,
    Tab,
    Tabs,
    Typography,
} from "@material-ui/core";
import { AxiosError } from "axios";
import React, { FunctionComponent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useSearchParams } from "react-router-dom";
import {
    NotificationTypeV1,
    PageContentsCardV1,
    SearchInputV1,
    SkeletonV1,
    useDialogProviderV1,
    useNotificationProviderV1,
} from "../../../platform/components";
import { DialogType } from "../../../platform/components/dialog-provider-v1/dialog-provider-v1.interfaces";
import { formatDateAndTimeV1 } from "../../../platform/utils";
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
import { EventsWizard } from "../../event-wizard/event-wizard.component";
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
}) => {
    const { notify } = useNotificationProviderV1();
    const [searchParams, setSearchParams] = useSearchParams();
    const { showDialog, hideDialog } = useDialogProviderV1();
    const { t } = useTranslation();
    const [selectedTabIndex, setSelectedTabIndex] = useState(() => {
        if (searchParams.has(ANALYSIS_TAB_IDX_KEY)) {
            return Number(searchParams.get(ANALYSIS_TAB_IDX_KEY));
        }

        return 1;
    });
    const [eventsSearchValue, setEventsSearchValue] = useState("");
    const [triggerUpdateEvents, setTriggerUpdateEvents] = useState(false);

    const [comparisonOffset, setComparisonOffset] = useState(() => {
        return searchParams.get(ANALYSIS_TAB_OFFSET) ?? "P1W";
    });

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

    const handleAddEventClick = (): void => {
        const event = createEmptyEvent();
        event.startTime = anomaly?.startTime as number;
        event.endTime = anomaly?.endTime as number;

        showDialog({
            width: "md",
            type: DialogType.CUSTOM,
            contents: (
                <EventsWizard
                    fullWidth
                    showCancel
                    event={event}
                    onCancel={hideDialog}
                    onSubmit={(newEvent: EditableEvent) => {
                        createEvent(newEvent)
                            .then((event: Event): void => {
                                notify(
                                    NotificationTypeV1.Success,
                                    t("message.create-success", {
                                        entity: t("label.event"),
                                    })
                                );
                                onEventSelectionChange([
                                    ...selectedEvents,
                                    event,
                                ]);
                                setTriggerUpdateEvents(!triggerUpdateEvents);
                                hideDialog();
                            })
                            .catch((error: AxiosError): void => {
                                const errMessages = getErrorMessages(error);

                                notifyIfErrors(
                                    ActionStatus.Error,
                                    errMessages,
                                    notify,
                                    t("message.create-error", {
                                        entity: t("label.event"),
                                    })
                                );
                            });
                    }}
                />
            ),
            hideCancelButton: true,
            hideOkButton: true,
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
                        <Grid
                            container
                            item
                            justifyContent="flex-end"
                            md={7}
                            sm={6}
                            xs={12}
                        >
                            <Grid item>
                                <Button
                                    color="primary"
                                    variant="outlined"
                                    onClick={handleAddEventClick}
                                >
                                    {t("label.add-entity", {
                                        entity: t("label.event"),
                                    })}
                                </Button>
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
                    )}
                </Grid>
            </CardContent>

            {anomaly && selectedTabIndex !== 2 && (
                <CardContent>
                    <Grid container>
                        <Grid item xs={12}>
                            <Typography variant="h6">Date Reference</Typography>
                        </Grid>
                        <Grid item xs={6}>
                            <div>
                                <strong>
                                    &quot;{t("label.current")}&quot;
                                </strong>{" "}
                                Data Date Range
                            </div>
                            <div>
                                {formatDateAndTimeV1(
                                    anomaly.startTime,
                                    timezone
                                )}
                                <strong> to </strong>
                                {formatDateAndTimeV1(anomaly.endTime, timezone)}
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
                                {formatDateAndTimeV1(
                                    anomaly.startTime -
                                        baselineOffsetToMilliseconds(
                                            comparisonOffset
                                        ),
                                    timezone
                                )}
                                <strong> to </strong>
                                {formatDateAndTimeV1(
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
                        timezone={timezone}
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
