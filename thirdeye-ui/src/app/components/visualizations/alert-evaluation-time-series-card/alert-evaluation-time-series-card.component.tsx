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
    CardHeader,
    FormHelperText,
    Grid,
} from "@material-ui/core";
import { PageContentsCardV1, SkeletonV1 } from "@startree-ui/platform-ui";
import React, { FunctionComponent, useCallback } from "react";
import { TimeRangeButtonWithContext } from "../../time-range/time-range-button-with-context/time-range-button.component";
import { AlertEvaluationTimeSeries } from "../alert-evaluation-time-series/alert-evaluation-time-series/alert-evaluation-time-series.component";
import { VisualizationCard } from "../visualization-card/visualization-card.component";
import { AlertEvaluationTimeSeriesCardProps } from "./alert-evaluation-time-series-card.interfaces";
import { useAlertEvaluationTimeSeriesCardStyles } from "./alert-evaluation-time-series-card.styles";

export const AlertEvaluationTimeSeriesCard: FunctionComponent<
    AlertEvaluationTimeSeriesCardProps
> = (props: AlertEvaluationTimeSeriesCardProps) => {
    const alertEvaluationTimeSeriesCardClasses =
        useAlertEvaluationTimeSeriesCardStyles();

    const handlePreview = useCallback(() => {
        props.onRefresh && props.onRefresh();
    }, [props.onRefresh]);

    if (props.isLoading) {
        return (
            <PageContentsCardV1>
                <SkeletonV1 animation="pulse" height={500} variant="rect" />
            </PageContentsCardV1>
        );
    }

    return (
        <Card variant="outlined">
            <CardHeader
                action={
                    <Grid container>
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

                        <Grid item>
                            <TimeRangeButtonWithContext
                                onTimeRangeChange={(
                                    start: number,
                                    end: number
                                ) =>
                                    props.onRefresh &&
                                    props.onRefresh(start, end)
                                }
                            />
                        </Grid>

                        {/* Preview button */}
                        {props.showPreviewButton && (
                            <Grid item>
                                <Box>
                                    <Button
                                        color="primary"
                                        variant="contained"
                                        onClick={handlePreview}
                                    >
                                        Preview
                                    </Button>
                                </Box>
                            </Grid>
                        )}
                    </Grid>
                }
                title={props.title}
                titleTypographyProps={{ variant: "h6" }}
            />

            <CardContent>
                <VisualizationCard
                    error={props.error}
                    helperText={props.helperText}
                    title={props.maximizedTitle || props.title}
                    visualizationHeight={props.alertEvaluationTimeSeriesHeight}
                    visualizationMaximizedHeight={
                        props.alertEvaluationTimeSeriesMaximizedHeight
                    }
                    onRefresh={props.onRefresh}
                >
                    <AlertEvaluationTimeSeries
                        alertEvaluation={props.alertEvaluation}
                        onAnomalyBarClick={props.onAnomalyBarClick}
                    />
                </VisualizationCard>
            </CardContent>
        </Card>
    );
};
