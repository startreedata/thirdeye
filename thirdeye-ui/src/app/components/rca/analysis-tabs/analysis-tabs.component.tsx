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
import {
    AnomalyBreakdownAPIOffsetValues,
    BASELINE_OPTIONS,
    OFFSET_TO_HUMAN_READABLE,
} from "../../../pages/anomalies-view-page/anomalies-view-page.interfaces";
import { formatDateAndTimeV1 } from "../../../platform/utils";
import { AnomalyBreakdownComparisonHeatmap } from "../../anomaly-breakdown-comparison-heatmap/anomaly-breakdown-comparison-heatmap.component";
import { useAnomalyBreakdownComparisonHeatmapStyles } from "../../anomaly-breakdown-comparison-heatmap/anomaly-breakdown-comparison-heatmap.styles";
import { OFFSET_TO_MILLISECONDS } from "../../anomaly-breakdown-comparison-heatmap/anomaly-breakdown-comparison-heatmap.utils";
import { AnomalyDimensionAnalysis } from "../../anomaly-dimension-analysis/anomaly-dimension-analysis.component";
import { AnalysisTabsProps } from "./analysis-tabs.interfaces";

export const AnalysisTabs: FunctionComponent<AnalysisTabsProps> = ({
    anomalyId,
    anomaly,
    onAddFilterSetClick,
}) => {
    const { t } = useTranslation();
    const classes = useAnomalyBreakdownComparisonHeatmapStyles();
    const [selectedTabIndex, setSelectedTabIndex] = useState(0);
    const [comparisonOffset, setComparisonWeekOffset] =
        useState<AnomalyBreakdownAPIOffsetValues>(
            AnomalyBreakdownAPIOffsetValues.ONE_WEEK_AGO
        );

    const onHandleComparisonOffsetSelection = (
        e: React.ChangeEvent<HTMLTextAreaElement | HTMLInputElement>
    ): void => {
        setComparisonWeekOffset(
            e.target.value as AnomalyBreakdownAPIOffsetValues
        );
    };

    const handleTabIndexChange = (_event: unknown, newValue: number): void => {
        setSelectedTabIndex(newValue);
    };

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
                                label="Heatmap of Change in Contribution"
                                value={0}
                            />
                            <Tab label="Dimension Analysis" value={1} />
                        </Tabs>
                    </Grid>
                    <Grid item md={5} sm={6} xs={12}>
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
                                            {t("label.baseline-week-offset")}:
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
                                    onChange={onHandleComparisonOffsetSelection}
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
                    </Grid>
                </Grid>
            </CardContent>

            <CardContent>
                {anomaly && (
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
                    <AnomalyBreakdownComparisonHeatmap
                        anomalyId={toNumber(anomalyId)}
                        comparisonOffset={comparisonOffset}
                        onAddFilterSetClick={onAddFilterSetClick}
                    />
                </Box>
            )}
            {selectedTabIndex === 1 && (
                <Box mt={-4}>
                    <AnomalyDimensionAnalysis
                        anomaly={anomaly}
                        anomalyId={toNumber(anomalyId)}
                        comparisonOffset={comparisonOffset}
                    />
                </Box>
            )}
        </Card>
    );
};
