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
import {
    Box,
    Button,
    Card,
    CardContent,
    Divider,
    Grid,
    Tab,
    Tabs,
    TextField,
    Typography,
} from "@material-ui/core";
import { Autocomplete } from "@material-ui/lab";
import { AxiosError } from "axios";
import { toNumber } from "lodash";
import React, { FunctionComponent, useEffect, useMemo, useState } from "react";
import { useTranslation } from "react-i18next";
import { useSearchParams } from "react-router-dom";
import {
    BaselineOptionsType,
    BASELINE_OPTIONS,
} from "../../../pages/anomalies-view-page/anomalies-view-page.interfaces";
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
    baselineComparisonOffsetToHumanReadable,
    parseBaselineComparisonOffset,
} from "../../../utils/anomaly-breakdown/anomaly-breakdown.util";
import { createEmptyEvent } from "../../../utils/events/events.util";
import { notifyIfErrors } from "../../../utils/notifications/notifications.util";
import { getErrorMessages } from "../../../utils/rest/rest.util";
import { AnomalyBreakdownComparisonHeatmap } from "../../anomaly-breakdown-comparison-heatmap/anomaly-breakdown-comparison-heatmap.component";
import { useAnomalyBreakdownComparisonHeatmapStyles } from "../../anomaly-breakdown-comparison-heatmap/anomaly-breakdown-comparison-heatmap.styles";
import { OFFSET_TO_MILLISECONDS } from "../../anomaly-breakdown-comparison-heatmap/anomaly-breakdown-comparison-heatmap.utils";
import { AnomalyDimensionAnalysis } from "../../anomaly-dimension-analysis/anomaly-dimension-analysis.component";
import { EventsWizard } from "../../event-wizard/event-wizard.component";
import { EventsTab } from "../events-tab/event-tab.component";
import { AnalysisTabsProps } from "./analysis-tabs.interfaces";

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
}) => {
    const { notify } = useNotificationProviderV1();
    const [searchParams, setSearchParams] = useSearchParams();
    const { showDialog, hideDialog } = useDialogProviderV1();
    const { t } = useTranslation();
    const classes = useAnomalyBreakdownComparisonHeatmapStyles();
    const [selectedTabIndex, setSelectedTabIndex] = useState(() => {
        if (searchParams.has(ANALYSIS_TAB_IDX_KEY)) {
            return Number(searchParams.get(ANALYSIS_TAB_IDX_KEY));
        }

        return 1;
    });
    const [eventsSearchValue, setEventsSearchValue] = useState("");
    const [triggerUpdateEvents, setTriggerUpdateEvents] = useState(false);
    const { baselineOffsetValue, unit } = parseBaselineComparisonOffset(
        searchParams.get(ANALYSIS_TAB_OFFSET) ?? ""
    );
    const [baselineOffsetUnit, setBaselineOffsetUnit] =
        useState<BaselineOptionsType>(
            BASELINE_OPTIONS.find((baseline) => baseline.key === unit) ||
                BASELINE_OPTIONS[0]
        );
    const [baselineValue, setBaselineValue] = useState(baselineOffsetValue);

    const handleBaselineOffsetUnitChange = (
        _event: React.ChangeEvent<Record<string, unknown>> | null,
        value: BaselineOptionsType | null
    ): void => {
        value && setBaselineOffsetUnit(value);
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

    const comparisonOffset = useMemo(() => {
        if (baselineValue && baselineOffsetUnit) {
            return `P${baselineValue}${baselineOffsetUnit.key}`;
        }

        return "P1W";
    }, [baselineValue, baselineOffsetUnit]);

    useEffect(() => {
        searchParams.set(ANALYSIS_TAB_OFFSET, comparisonOffset);
        setSearchParams(searchParams);
    }, [comparisonOffset]);

    useEffect(() => {
        setBaselineValue(baselineOffsetValue);
    }, [baselineOffsetValue]);

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
                            <Grid
                                container
                                justifyContent="flex-end"
                                spacing={2}
                            >
                                <Grid item xs={6}>
                                    <Box
                                        className={
                                            classes.baselineWeekOffsetLabelContainer
                                        }
                                        p="10.5px 0"
                                    >
                                        <label>
                                            <strong>
                                                {t("label.baseline-offset")}:
                                            </strong>
                                        </label>
                                    </Box>
                                </Grid>
                                <Grid item xs={6}>
                                    <Grid
                                        container
                                        direction="row"
                                        justifyContent="flex-end"
                                        spacing={2}
                                    >
                                        <Grid item xs={6}>
                                            <TextField
                                                required
                                                size="small"
                                                type="number"
                                                value={baselineValue}
                                                onChange={(e) =>
                                                    setBaselineValue(
                                                        Number(e.target.value)
                                                    )
                                                }
                                            />
                                        </Grid>
                                        <Grid item xs={6}>
                                            <Autocomplete<BaselineOptionsType>
                                                autoSelect
                                                classes={{
                                                    inputRoot: classes.input,
                                                }}
                                                getOptionLabel={(option) =>
                                                    option.description
                                                }
                                                options={BASELINE_OPTIONS}
                                                renderInput={(params) => (
                                                    <TextField
                                                        {...params}
                                                        InputProps={{
                                                            ...params.InputProps,
                                                        }}
                                                        variant="outlined"
                                                    />
                                                )}
                                                value={baselineOffsetUnit}
                                                onChange={
                                                    handleBaselineOffsetUnitChange
                                                }
                                            />
                                        </Grid>
                                    </Grid>
                                </Grid>
                            </Grid>
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
                                {formatDateAndTimeV1(anomaly.startTime)}
                                <strong> to </strong>
                                {formatDateAndTimeV1(anomaly.endTime)}
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
                                    {baselineComparisonOffsetToHumanReadable(
                                        baselineValue,
                                        baselineOffsetUnit.key
                                    )}
                                    )
                                </span>
                            </div>
                            <div>
                                {formatDateAndTimeV1(
                                    anomaly.startTime -
                                        baselineValue *
                                            OFFSET_TO_MILLISECONDS[
                                                baselineOffsetUnit.key
                                            ]
                                )}
                                <strong> to </strong>
                                {formatDateAndTimeV1(
                                    anomaly.endTime -
                                        baselineValue *
                                            OFFSET_TO_MILLISECONDS[
                                                baselineOffsetUnit.key
                                            ]
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
                        anomalyId={toNumber(anomalyId)}
                        chartTimeSeriesFilterSet={chartTimeSeriesFilterSet}
                        comparisonOffset={comparisonOffset}
                        onCheckClick={onAddFilterSetClick}
                    />
                </Box>
            )}
            {selectedTabIndex === 1 && (
                <Box mt={-4}>
                    <AnomalyBreakdownComparisonHeatmap
                        anomalyId={toNumber(anomalyId)}
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
                        triggerUpdate={triggerUpdateEvents}
                        onCheckClick={onEventSelectionChange}
                    />
                </Box>
            )}
        </Card>
    );
};
