import {
    Box,
    Card,
    CardContent,
    Divider,
    Grid,
    MenuItem,
    Tab,
    Tabs,
    TextField,
    Typography,
} from "@material-ui/core";
import { toNumber } from "lodash";
import React, { FunctionComponent, useState } from "react";
import { useTranslation } from "react-i18next";
import { useSearchParams } from "react-router-dom";
import {
    AnomalyBreakdownAPIOffsetValues,
    BASELINE_OPTIONS,
    OFFSET_TO_HUMAN_READABLE,
} from "../../../pages/anomalies-view-page/anomalies-view-page.interfaces";
import { PageContentsCardV1, SkeletonV1 } from "../../../platform/components";
import { formatDateAndTimeV1 } from "../../../platform/utils";
import { Anomaly } from "../../../rest/dto/anomaly.interfaces";
import { AnomalyBreakdownComparisonHeatmap } from "../../anomaly-breakdown-comparison-heatmap/anomaly-breakdown-comparison-heatmap.component";
import { useAnomalyBreakdownComparisonHeatmapStyles } from "../../anomaly-breakdown-comparison-heatmap/anomaly-breakdown-comparison-heatmap.styles";
import { OFFSET_TO_MILLISECONDS } from "../../anomaly-breakdown-comparison-heatmap/anomaly-breakdown-comparison-heatmap.utils";
import { AnomalyDimensionAnalysis } from "../../anomaly-dimension-analysis/anomaly-dimension-analysis.component";
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
    const [searchParams, setSearchParams] = useSearchParams();
    const { t } = useTranslation();
    const classes = useAnomalyBreakdownComparisonHeatmapStyles();
    const [selectedTabIndex, setSelectedTabIndex] = useState(
        Number(searchParams.get(ANALYSIS_TAB_IDX_KEY)) || 0
    );
    const [comparisonOffset, setComparisonWeekOffset] =
        useState<AnomalyBreakdownAPIOffsetValues>(
            (searchParams.get(
                ANALYSIS_TAB_OFFSET
            ) as AnomalyBreakdownAPIOffsetValues) ||
                AnomalyBreakdownAPIOffsetValues.ONE_WEEK_AGO
        );

    const onHandleComparisonOffsetSelection = (
        e: React.ChangeEvent<HTMLTextAreaElement | HTMLInputElement>
    ): void => {
        setComparisonWeekOffset(
            e.target.value as AnomalyBreakdownAPIOffsetValues
        );
        searchParams.set(ANALYSIS_TAB_OFFSET, e.target.value);
        setSearchParams(searchParams);
    };

    const handleTabIndexChange = (_event: unknown, newValue: number): void => {
        setSelectedTabIndex(newValue);
        searchParams.set(ANALYSIS_TAB_IDX_KEY, newValue.toString());
        setSearchParams(searchParams);
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
                <Grid container justifyContent="space-between">
                    <Grid item md={7} sm={6} xs={12}>
                        <Tabs
                            value={selectedTabIndex}
                            onChange={handleTabIndexChange}
                        >
                            <Tab
                                label={t("label.top-contributors")}
                                value={0}
                            />
                            <Tab label={t("label.heatmap")} value={1} />
                            <Tab label={t("label.events")} value={2} />
                        </Tabs>
                    </Grid>
                    <Grid item md={5} sm={6} xs={12}>
                        {/* Hide baseline offset selector if events tab is selected */}
                        {selectedTabIndex !== 2 && (
                            <Grid container spacing={0}>
                                <Grid item sm={6} xs={12}>
                                    <Box
                                        className={
                                            classes.baselineWeekOffsetLabelContainer
                                        }
                                        p="10.5px 0"
                                    >
                                        <label>
                                            <strong>
                                                {t(
                                                    "label.baseline-week-offset"
                                                )}
                                                :
                                            </strong>
                                        </label>
                                    </Box>
                                </Grid>
                                <Grid item sm={6} xs={12}>
                                    <TextField
                                        fullWidth
                                        select
                                        size="small"
                                        value={comparisonOffset}
                                        onChange={
                                            onHandleComparisonOffsetSelection
                                        }
                                    >
                                        {BASELINE_OPTIONS.map((option) => (
                                            <MenuItem
                                                key={option.key}
                                                value={option.key}
                                            >
                                                {option.description}
                                            </MenuItem>
                                        ))}
                                    </TextField>
                                </Grid>
                            </Grid>
                        )}
                    </Grid>
                </Grid>
            </CardContent>

            <CardContent>
                {anomaly && selectedTabIndex !== 2 && (
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
                                    {OFFSET_TO_HUMAN_READABLE[comparisonOffset]}
                                    )
                                </span>
                            </div>
                            <div>
                                {formatDateAndTimeV1(
                                    anomaly.startTime -
                                        OFFSET_TO_MILLISECONDS[comparisonOffset]
                                )}
                                <strong> to </strong>
                                {formatDateAndTimeV1(
                                    anomaly.endTime -
                                        OFFSET_TO_MILLISECONDS[comparisonOffset]
                                )}
                            </div>
                        </Grid>

                        <Grid item xs={12}>
                            <Divider />
                        </Grid>
                    </Grid>
                )}
            </CardContent>
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
                <Box mt={-4}>
                    <EventsTab
                        anomalyId={anomalyId}
                        selectedEvents={selectedEvents}
                        onCheckClick={onEventSelectionChange}
                    />
                </Box>
            )}
        </Card>
    );
};
