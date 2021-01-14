import {
    Box,
    Button,
    Grid,
    Step,
    StepLabel,
    Stepper,
    Typography,
} from "@material-ui/core";
import { kebabCase } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Alert } from "../../rest/dto/alert.interfaces";
import { createDefaultAlert } from "../../utils/alerts-util/alerts-util";
import { Dimension } from "../../utils/material-ui-util/dimension-util";
import { Palette } from "../../utils/material-ui-util/palette-util";
import { validateJSON } from "../../utils/validation-util/validation-util";
import { JSONEditor } from "../json-editor/json-editor.component";
import { AlertWizardProps, AlertWizardStep } from "./alert-wizard.interfaces";
import { useAlertWizardStyles } from "./alert-wizard.styles";

export const AlertWizard: FunctionComponent<AlertWizardProps> = (
    props: AlertWizardProps
) => {
    const alertWizardClasses = useAlertWizardStyles();
    const [newAlert, setNewAlert] = useState<Alert>(
        props.alert || createDefaultAlert()
    );
    const [
        detectionConfigurationError,
        setDetectionConfigurationError,
    ] = useState(false);
    const [
        detectionConfigurationHelperText,
        setDetectionConfigurationHelperText,
    ] = useState("");
    const [currentWizardStep, setCurrentWizardStep] = useState<AlertWizardStep>(
        AlertWizardStep.DETECTION_CONFIGURATION
    );
    const { t } = useTranslation();

    useEffect(() => {
        // Notify
        props.onChange && props.onChange(currentWizardStep);
    }, [currentWizardStep]);

    const onDetectionConfigurationChange = (value: string): void => {
        let validationResult;
        if (
            (validationResult = validateJSON(value)) &&
            !validationResult.valid
        ) {
            // Validation failed
            setDetectionConfigurationError(true);
            setDetectionConfigurationHelperText(validationResult.message || "");

            return;
        }

        setDetectionConfigurationError(false);
        setDetectionConfigurationHelperText("");
        setNewAlert(JSON.parse(value));
    };

    const onCancel = (): void => {
        props.onCancel && props.onCancel();
    };

    const onBack = (): void => {
        if (currentWizardStep === AlertWizardStep.DETECTION_CONFIGURATION) {
            // Already on first step
            return;
        }

        // Calculate previous step
        setCurrentWizardStep(
            AlertWizardStep[
                AlertWizardStep[
                    currentWizardStep - 1
                ] as keyof typeof AlertWizardStep
            ]
        );
    };

    const onNext = (): void => {
        if (detectionConfigurationError) {
            return;
        }

        if (currentWizardStep === AlertWizardStep.REVIEW_AND_SUBMIT) {
            // On last step
            props.onFinish && props.onFinish(newAlert);

            return;
        }

        // Calculate next step
        setCurrentWizardStep(
            AlertWizardStep[
                AlertWizardStep[
                    currentWizardStep + 1
                ] as keyof typeof AlertWizardStep
            ]
        );
    };

    return (
        <>
            <Grid container>
                {/* Stepper */}
                <Grid item md={12}>
                    <Stepper alternativeLabel activeStep={currentWizardStep}>
                        {Object.values(AlertWizardStep)
                            .filter(
                                (alertWizardStep) =>
                                    typeof alertWizardStep === "string"
                            )
                            .map((alertWizardStep, index) => (
                                <Step key={index}>
                                    <StepLabel>
                                        {t(
                                            `label.${kebabCase(
                                                alertWizardStep as string
                                            )}`
                                        )}
                                    </StepLabel>
                                </Step>
                            ))}
                    </Stepper>
                </Grid>

                {/* Step label */}
                <Grid item md={12}>
                    <Typography variant="h5">
                        {t(
                            `label.${kebabCase(
                                AlertWizardStep[currentWizardStep]
                            )}`
                        )}
                    </Typography>
                </Grid>

                {/* Spacer */}
                <Grid item md={12} />

                {/* Detection configuration */}
                {currentWizardStep ===
                    AlertWizardStep.DETECTION_CONFIGURATION && (
                    <>
                        {/* Detection configuration editor */}
                        <Grid item md={12}>
                            <JSONEditor
                                error={detectionConfigurationError}
                                helperText={detectionConfigurationHelperText}
                                value={
                                    (newAlert as unknown) as Record<
                                        string,
                                        unknown
                                    >
                                }
                                onChange={onDetectionConfigurationChange}
                            />
                        </Grid>
                    </>
                )}

                {/* Review and submit */}
                {currentWizardStep === AlertWizardStep.REVIEW_AND_SUBMIT && (
                    <>{/* Alert information */}</>
                )}
            </Grid>

            {/* Spacer */}
            <Box padding={2} />

            {/* Controls */}
            <Grid
                container
                alignItems="stretch"
                className={alertWizardClasses.controlsContainer}
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
                                            AlertWizardStep.DETECTION_CONFIGURATION
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
                                    <Button
                                        color="primary"
                                        disabled={detectionConfigurationError}
                                        size="large"
                                        variant="contained"
                                        onClick={onNext}
                                    >
                                        {currentWizardStep ===
                                        AlertWizardStep.REVIEW_AND_SUBMIT
                                            ? t("label.finish")
                                            : t("label.next")}
                                    </Button>
                                </Grid>
                            </Grid>
                        </Grid>
                    </Grid>
                </Grid>
            </Grid>
        </>
    );
};
