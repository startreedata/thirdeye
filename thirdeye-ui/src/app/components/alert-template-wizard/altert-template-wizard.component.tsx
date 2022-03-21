import { Box, Button, Grid, Typography } from "@material-ui/core";
import { Alert as MuiAlert } from "@material-ui/lab";
import { kebabCase } from "lodash";
import React, { useState } from "react";
import { useTranslation } from "react-i18next";
import {
    JSONEditorV1,
    PageContentsCardV1,
    StepperV1,
} from "../../platform/components";
import { createDefaultAlertTemplate } from "../../utils/alert-templates/alert-templates.util";
import { Dimension } from "../../utils/material-ui/dimension.util";
import { Palette } from "../../utils/material-ui/palette.util";
import { validateJSON } from "../../utils/validation/validation.util";
import {
    AlertTemplateWizardProps,
    AlertTemplateWizardStep,
} from "./altert-template-wizard.interfaces";
import { useAlertTemplateWizardStyles } from "./altert-template-wizard.styles";

function AlertTemplateWizard<NewOrExistingTemplate>(
    props: AlertTemplateWizardProps<NewOrExistingTemplate>
): JSX.Element {
    const alertTemplateWizardClasses = useAlertTemplateWizardStyles();
    const [newAlertTemplate, setNewAlertTemplate] =
        useState<NewOrExistingTemplate>(props.alertTemplate);
    const [newAlertTemplateJSON, setNewAlertTemplateJSON] = useState(
        JSON.stringify(props.alertTemplate)
    );
    const [
        alertTemplateConfigurationError,
        setAlertTemplateConfigurationError,
    ] = useState(false);
    const [
        alertTemplateConfigurationHelperText,
        setAlertTemplateConfigurationHelperText,
    ] = useState("");
    const [currentWizardStep, setCurrentWizardStep] =
        useState<AlertTemplateWizardStep>(
            AlertTemplateWizardStep.ALERT_TEMPLATE_CONFIGURATION
        );
    const { t } = useTranslation();

    const onAlertTemplateChange = (value: string): void => {
        setNewAlertTemplateJSON(value);
    };

    const onCancel = (): void => {
        props.onCancel && props.onCancel();
    };

    const onBack = (): void => {
        if (
            currentWizardStep ===
            AlertTemplateWizardStep.ALERT_TEMPLATE_CONFIGURATION
        ) {
            // Already on first step
            return;
        }

        // Determine previous step
        setCurrentWizardStep(
            AlertTemplateWizardStep[
                AlertTemplateWizardStep[
                    currentWizardStep - 1
                ] as keyof typeof AlertTemplateWizardStep
            ]
        );
    };

    const onNext = (): void => {
        if (
            currentWizardStep ===
                AlertTemplateWizardStep.ALERT_TEMPLATE_CONFIGURATION &&
            !validateDatasourceConfiguration()
        ) {
            return;
        }

        if (currentWizardStep === AlertTemplateWizardStep.REVIEW_AND_SUBMIT) {
            // On last step
            props.onFinish && props.onFinish(newAlertTemplate);

            return;
        }

        // Determine next step
        setCurrentWizardStep(
            AlertTemplateWizardStep[
                AlertTemplateWizardStep[
                    currentWizardStep + 1
                ] as keyof typeof AlertTemplateWizardStep
            ]
        );
    };

    const validateDatasourceConfiguration = (): boolean => {
        let validationResult;
        if (
            (validationResult = validateJSON(newAlertTemplateJSON)) &&
            !validationResult.valid
        ) {
            // Validation failed
            setAlertTemplateConfigurationError(true);
            setAlertTemplateConfigurationHelperText(
                validationResult.message || ""
            );

            return false;
        }

        setAlertTemplateConfigurationError(false);
        setAlertTemplateConfigurationHelperText("");
        setNewAlertTemplate(JSON.parse(newAlertTemplateJSON));

        return true;
    };

    const onReset = (): void => {
        const alertTemplate = props.alertTemplate
            ? ({
                  ...props.alertTemplate,
              } as NewOrExistingTemplate)
            : createDefaultAlertTemplate();
        setNewAlertTemplate(alertTemplate as NewOrExistingTemplate);
        setNewAlertTemplateJSON(JSON.stringify(alertTemplate));
    };

    const stepLabelFn = (step: string): string => {
        return t(`label.${kebabCase(AlertTemplateWizardStep[+step])}`);
    };

    return (
        <>
            {/* Stepper */}
            <Grid container>
                <Grid item sm={12}>
                    <StepperV1
                        activeStep={currentWizardStep.toString()}
                        stepLabelFn={stepLabelFn}
                        steps={Object.values(AlertTemplateWizardStep).reduce(
                            (steps, alertTemplateWizardStep) => {
                                if (
                                    typeof alertTemplateWizardStep === "number"
                                ) {
                                    steps.push(
                                        alertTemplateWizardStep.toString()
                                    );
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
                                    AlertTemplateWizardStep[currentWizardStep]
                                )}`
                            )}
                        </Typography>
                    </Grid>

                    {/* Spacer */}
                    <Grid item sm={12} />

                    {/* Datasource configuration */}
                    {currentWizardStep ===
                        AlertTemplateWizardStep.ALERT_TEMPLATE_CONFIGURATION && (
                        <>
                            {/* Datasource configuration editor */}
                            <Grid item sm={12}>
                                <JSONEditorV1<NewOrExistingTemplate>
                                    hideValidationSuccessIcon
                                    error={alertTemplateConfigurationError}
                                    helperText={
                                        alertTemplateConfigurationHelperText
                                    }
                                    value={newAlertTemplate}
                                    onChange={onAlertTemplateChange}
                                />
                            </Grid>
                        </>
                    )}

                    {/* Review and submit */}
                    {currentWizardStep ===
                        AlertTemplateWizardStep.REVIEW_AND_SUBMIT && (
                        <>
                            {/* Datasource information */}
                            <Grid item sm={12}>
                                <JSONEditorV1<NewOrExistingTemplate>
                                    hideValidationSuccessIcon
                                    readOnly
                                    value={newAlertTemplate}
                                />
                            </Grid>
                        </>
                    )}
                </Grid>

                {/* Controls */}
                <Grid
                    container
                    alignItems="stretch"
                    className={alertTemplateWizardClasses.controlsContainer}
                    justify="flex-end"
                >
                    {alertTemplateConfigurationError && (
                        <Grid item sm={12}>
                            <MuiAlert severity="error">
                                There were some errors
                            </MuiAlert>
                        </Grid>
                    )}

                    {/* Separator */}
                    <Grid item sm={12}>
                        <Box
                            border={Dimension.WIDTH_BORDER_DEFAULT}
                            borderBottom={0}
                            borderColor={Palette.COLOR_BORDER_DEFAULT}
                            borderLeft={0}
                            borderRight={0}
                        />
                    </Grid>

                    <Grid item sm={12}>
                        <Grid container justify="space-between">
                            {/* Cancel button */}
                            <Grid item>
                                <Grid container>
                                    {props.showCancel && (
                                        <Grid item>
                                            <Button
                                                color="primary"
                                                size="large"
                                                variant="outlined"
                                                onClick={onCancel}
                                            >
                                                {t("label.cancel")}
                                            </Button>
                                        </Grid>
                                    )}

                                    {currentWizardStep ===
                                        AlertTemplateWizardStep.ALERT_TEMPLATE_CONFIGURATION && (
                                        <Grid item>
                                            <Button
                                                color="primary"
                                                size="large"
                                                variant="outlined"
                                                onClick={onReset}
                                            >
                                                Reset
                                            </Button>
                                        </Grid>
                                    )}
                                </Grid>
                            </Grid>

                            <Grid item>
                                <Grid container>
                                    {/* Back button */}
                                    <Grid item>
                                        <Button
                                            color="primary"
                                            disabled={
                                                currentWizardStep ===
                                                AlertTemplateWizardStep.ALERT_TEMPLATE_CONFIGURATION
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
                                            size="large"
                                            variant="contained"
                                            onClick={onNext}
                                        >
                                            {currentWizardStep ===
                                            AlertTemplateWizardStep.REVIEW_AND_SUBMIT
                                                ? t("label.finish")
                                                : t("label.next")}
                                        </Button>
                                    </Grid>
                                </Grid>
                            </Grid>
                        </Grid>
                    </Grid>
                </Grid>
            </PageContentsCardV1>
        </>
    );
}

export { AlertTemplateWizard };
