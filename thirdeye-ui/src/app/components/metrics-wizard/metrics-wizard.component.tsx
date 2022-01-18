import { Box, Button, Grid, Typography } from "@material-ui/core";
import { PageContentsCardV1, StepperV1 } from "@startree-ui/platform-ui";
import { kebabCase } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { LogicalMetric } from "../../rest/dto/metric.interfaces";
import { Dimension } from "../../utils/material-ui/dimension.util";
import { Palette } from "../../utils/material-ui/palette.util";
import { createEmptyMetric } from "../../utils/metrics/metrics.util";
import { MetricsPropertiesForm } from "../metrics-wizard/metrics-properties-form/metrics-renderer-form.component";
import { MetricRenderer } from "../metrics-wizard/metrics-renderer/metrics-renderer.component";
import {
    MetricsWizardProps,
    MetricsWizardStep,
} from "./metrics-wizard.interfaces";
import { useMetricsWizardStyles } from "./metrics-wizard.styles";

const FORM_ID_METRICS_PROPERTIES = "FORM_ID_METRICS_PROPERTIES";

export const MetricsWizard: FunctionComponent<MetricsWizardProps> = (
    props: MetricsWizardProps
) => {
    const metricWizardClasses = useMetricsWizardStyles();
    const [newMetric, setNewMetric] = useState<LogicalMetric>(
        props.metric || createEmptyMetric()
    );
    const [
        currentWizardStep,
        setCurrentWizardStep,
    ] = useState<MetricsWizardStep>(MetricsWizardStep.METRIC_PROPERTIES);
    const { t } = useTranslation();

    useEffect(() => {
        // Notify
        props.onChange && props.onChange(currentWizardStep);
    }, [currentWizardStep]);

    const onSubmitMetricPropertiesForm = (metric: LogicalMetric): void => {
        // Update metric with form inputs
        setNewMetric((newMetrics) => Object.assign(newMetrics, metric));

        // Next step
        onNext();
    };

    const onCancel = (): void => {
        props.onCancel && props.onCancel();
    };

    const onBack = (): void => {
        if (currentWizardStep === MetricsWizardStep.METRIC_PROPERTIES) {
            // Already on first step
            return;
        }

        // Determine previous step
        setCurrentWizardStep(
            MetricsWizardStep[
                MetricsWizardStep[
                    currentWizardStep - 1
                ] as keyof typeof MetricsWizardStep
            ]
        );
    };

    const onNext = (): void => {
        if (currentWizardStep === MetricsWizardStep.REVIEW_AND_SUBMIT) {
            // On last step
            props.onFinish && props.onFinish(newMetric);

            return;
        }

        // Determine next step
        setCurrentWizardStep(
            MetricsWizardStep[
                MetricsWizardStep[
                    currentWizardStep + 1
                ] as keyof typeof MetricsWizardStep
            ]
        );
    };

    const stepLabelFn = (step: string): string => {
        return t(`label.${kebabCase(MetricsWizardStep[+step])}`);
    };

    return (
        <>
            {/* Stepper */}
            <Grid container>
                <Grid item sm={12}>
                    <StepperV1
                        activeStep={currentWizardStep.toString()}
                        stepLabelFn={stepLabelFn}
                        steps={Object.values(MetricsWizardStep).reduce(
                            (steps, metricWizardStep) => {
                                if (typeof metricWizardStep === "number") {
                                    steps.push(metricWizardStep.toString());
                                }

                                return steps;
                            },
                            [] as string[]
                        )}
                    />
                </Grid>
            </Grid>

            <PageContentsCardV1>
                <Grid container>
                    {/* Step label */}
                    <Grid item sm={12}>
                        <Typography variant="h5">
                            {t(
                                `label.${kebabCase(
                                    MetricsWizardStep[currentWizardStep]
                                )}`
                            )}
                        </Typography>
                    </Grid>

                    {/* Spacer */}
                    <Grid item sm={12} />

                    {/* Metric properties */}
                    {currentWizardStep ===
                        MetricsWizardStep.METRIC_PROPERTIES && (
                        <>
                            {/* Metric properties form */}
                            <Grid item sm={12}>
                                <MetricsPropertiesForm
                                    datasets={props.datasets}
                                    id={FORM_ID_METRICS_PROPERTIES}
                                    metric={newMetric}
                                    onSubmit={onSubmitMetricPropertiesForm}
                                />
                            </Grid>

                            {/* Spacer */}
                            <Grid item sm={12} />
                        </>
                    )}

                    {/* Review and submit */}
                    {currentWizardStep ===
                        MetricsWizardStep.REVIEW_AND_SUBMIT && (
                        <>
                            {/* Metrics information */}
                            <MetricRenderer metric={newMetric} />
                        </>
                    )}
                </Grid>

                {/* Spacer */}
                <Box padding={2} />

                {/* Controls */}
                <Grid
                    container
                    alignItems="stretch"
                    className={metricWizardClasses.controlsContainer}
                    direction="column"
                    justify="flex-end"
                >
                    {/* Separator */}
                    <Grid item>
                        <Box
                            border={Dimension.WIDTH_BORDER_DEFAULT}
                            borderBottom={0}
                            borderColor={Palette.COLOR_BORDER_DEFAULT}
                            borderLeft={0}
                            borderRight={0}
                        />
                    </Grid>

                    <Grid item>
                        <Grid container justify="space-between">
                            {/* Cancel button */}
                            <Grid item>
                                {props.showCancel && (
                                    <Button
                                        color="primary"
                                        size="large"
                                        variant="outlined"
                                        onClick={onCancel}
                                    >
                                        {t("label.cancel")}
                                    </Button>
                                )}
                            </Grid>

                            <Grid item>
                                <Grid container>
                                    {/* Back button */}
                                    <Grid item>
                                        <Button
                                            color="primary"
                                            disabled={
                                                currentWizardStep ===
                                                MetricsWizardStep.METRIC_PROPERTIES
                                            }
                                            size="large"
                                            variant="outlined"
                                            onClick={onBack}
                                        >
                                            {t("label.back")}
                                        </Button>
                                    </Grid>

                                    {/* Next button */}
                                    <Grid item>
                                        {/* Submit button for 
                                        metric properties form in first step */}
                                        {currentWizardStep ===
                                            MetricsWizardStep.METRIC_PROPERTIES && (
                                            <Button
                                                color="primary"
                                                form={
                                                    FORM_ID_METRICS_PROPERTIES
                                                }
                                                size="large"
                                                type="submit"
                                                variant="contained"
                                            >
                                                {t("label.next")}
                                            </Button>
                                        )}

                                        {/* Next button for all other steps */}
                                        {currentWizardStep !==
                                            MetricsWizardStep.METRIC_PROPERTIES && (
                                            <Button
                                                color="primary"
                                                size="large"
                                                variant="contained"
                                                onClick={onNext}
                                            >
                                                {currentWizardStep ===
                                                MetricsWizardStep.REVIEW_AND_SUBMIT
                                                    ? t("label.finish")
                                                    : t("label.next")}
                                            </Button>
                                        )}
                                    </Grid>
                                </Grid>
                            </Grid>
                        </Grid>
                    </Grid>
                </Grid>
            </PageContentsCardV1>
        </>
    );
};
