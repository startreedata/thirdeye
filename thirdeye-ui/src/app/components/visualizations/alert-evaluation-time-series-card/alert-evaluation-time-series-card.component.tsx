import {
    Button,
    Card,
    CardContent,
    CardHeader,
    FormHelperText,
    Grid,
    IconButton,
    Popover,
} from "@material-ui/core";
import DateRangeIcon from "@material-ui/icons/DateRange";
import FullscreenExitIcon from "@material-ui/icons/FullscreenExit";
import RefreshIcon from "@material-ui/icons/Refresh";
import React, { FunctionComponent, useEffect, useState } from "react";
import { formatTimeRangeDuration } from "../../../utils/time-range/time-range.util";
import { useTimeRange } from "../../time-range/time-range-provider/time-range-provider.component";
import { TimeRangeSelectorPopoverContent } from "../../time-range/time-range-selector-popover-content/time-range-selector-popover-content.component";
import { AlertEvaluationTimeSeries } from "../alert-evaluation-time-series/alert-evaluation-time-series/alert-evaluation-time-series.component";
import { VisualizationCard } from "../visualization-card/visualization-card.component";
import { AlertEvaluationTimeSeriesCardProps } from "./alert-evaluation-time-series-card.interfaces";
import { useAlertEvaluationTimeSeriesCardStyles } from "./alert-evaluation-time-series-card.styles";

export const AlertEvaluationTimeSeriesCard: FunctionComponent<AlertEvaluationTimeSeriesCardProps> = ({
    hideRangeControls = true,
    ...props
}: AlertEvaluationTimeSeriesCardProps) => {
    const alertEvaluationTimeSeriesCardClasses = useAlertEvaluationTimeSeriesCardStyles();
    const [maximized, setMaximized] = useState(props.maximized);

    // For the time range selector
    const [
        timeRangeSelectorAnchorElement,
        setTimeRangeSelectorAnchorElement,
    ] = useState<HTMLElement | null>();
    const {
        timeRangeDuration,
        setTimeRangeDuration,
        recentCustomTimeRangeDurations,
    } = useTimeRange();
    const handleTimeRangeSelectorClose = (): void => {
        setTimeRangeSelectorAnchorElement(null);
    };

    useEffect(() => {
        // Maximize/restore input changed, update
        setMaximized(props.maximized);
    }, [props.maximized]);

    const handleAlertEvaluationTimeSeriesCardMaximize = (): void => {
        setMaximized(true);
    };

    const handleAlertEvaluationTimeSeriesCardRestore = (): void => {
        setMaximized(false);
    };

    return (
        <Card variant="outlined">
            <CardHeader
                action={
                    <Grid container alignItems="center" spacing={0}>
                        {/* Helper text */}
                        {props.helperText && (
                            <Grid item>
                                <FormHelperText
                                    className={
                                        alertEvaluationTimeSeriesCardClasses.helperText
                                    }
                                    error={props.error}
                                >
                                    {props.helperText}
                                </FormHelperText>
                            </Grid>
                        )}

                        {/* Date range edit button */}
                        {!hideRangeControls && (
                            <Grid item>
                                <Button
                                    color="secondary"
                                    endIcon={<DateRangeIcon />}
                                    variant="text"
                                    onClick={(e) =>
                                        setTimeRangeSelectorAnchorElement(
                                            e.currentTarget
                                        )
                                    }
                                >
                                    {formatTimeRangeDuration(timeRangeDuration)}
                                </Button>
                                {/* Time range selector */}
                                <Popover
                                    anchorEl={timeRangeSelectorAnchorElement}
                                    open={Boolean(
                                        timeRangeSelectorAnchorElement
                                    )}
                                    onClose={handleTimeRangeSelectorClose}
                                >
                                    <TimeRangeSelectorPopoverContent
                                        recentCustomTimeRangeDurations={
                                            recentCustomTimeRangeDurations
                                        }
                                        timeRangeDuration={timeRangeDuration}
                                        onChange={setTimeRangeDuration}
                                        onClose={handleTimeRangeSelectorClose}
                                    />
                                </Popover>
                            </Grid>
                        )}

                        {/* Refresh button */}
                        {!props.hideRefreshButton && (
                            <Grid item>
                                <IconButton
                                    color="secondary"
                                    onClick={props.onRefresh}
                                >
                                    <RefreshIcon />
                                </IconButton>
                            </Grid>
                        )}

                        {/* Maximize button */}
                        <Grid item>
                            <IconButton
                                color="secondary"
                                onClick={
                                    handleAlertEvaluationTimeSeriesCardMaximize
                                }
                            >
                                <FullscreenExitIcon />
                            </IconButton>
                        </Grid>
                    </Grid>
                }
                title={props.title}
                titleTypographyProps={{ variant: "h6" }}
            />

            <CardContent>
                <VisualizationCard
                    error={props.error}
                    helperText={props.helperText}
                    hideRefreshButton={props.hideRefreshButton}
                    maximized={maximized}
                    title={props.maximizedTitle || props.title}
                    visualizationHeight={props.alertEvaluationTimeSeriesHeight}
                    visualizationMaximizedHeight={
                        props.alertEvaluationTimeSeriesMaximizedHeight
                    }
                    onRefresh={props.onRefresh}
                    onRestore={handleAlertEvaluationTimeSeriesCardRestore}
                >
                    <AlertEvaluationTimeSeries
                        hideBrush
                        alertEvaluation={props.alertEvaluation}
                    />
                </VisualizationCard>
            </CardContent>
        </Card>
    );
};
