import { Box, Button, Grid, Typography } from "@material-ui/core";
import { Alert as MuiAlert } from "@material-ui/lab";
import {
    JSONEditorV1,
    PageContentsCardV1,
    StepperV1,
} from "@startree-ui/platform-ui";
import { kebabCase } from "lodash";
import React, { FunctionComponent, useState } from "react";
import { useTranslation } from "react-i18next";
import { Datasource } from "../../rest/dto/datasource.interfaces";
import { createDefaultDatasource } from "../../utils/datasources/datasources.util";
import { Dimension } from "../../utils/material-ui/dimension.util";
import { Palette } from "../../utils/material-ui/palette.util";
import { validateJSON } from "../../utils/validation/validation.util";
import {
    DatasourceWizardProps,
    DatasourceWizardStep,
} from "./datasource-wizard.interfaces";
import { useDatasourceWizardStyles } from "./datasource-wizard.styles";

export const DatasourceWizard: FunctionComponent<DatasourceWizardProps> = (
    props: DatasourceWizardProps
) => {
    const datasourceWizardClasses = useDatasourceWizardStyles();
    const [newDatasource, setNewDatasource] = useState<Datasource>(
        props.datasource || createDefaultDatasource()
    );
    const [newDatasourceJSON, setNewDatasourceJSON] = useState(
        JSON.stringify(props.datasource || createDefaultDatasource())
    );
    const [
        datasourceConfigurationError,
        setDatasourceConfigurationError,
    ] = useState(false);
    const [
        datasourceConfigurationHelperText,
        setDatasourceConfigurationHelperText,
    ] = useState("");
    const [
        currentWizardStep,
        setCurrentWizardStep,
    ] = useState<DatasourceWizardStep>(
        DatasourceWizardStep.DATASOURCE_CONFIGURATION
    );
    const { t } = useTranslation();

    const onDatasourceConfigurationChange = (value: string): void => {
        setNewDatasourceJSON(value);
    };

    const onCancel = (): void => {
        props.onCancel && props.onCancel();
    };

    const onBack = (): void => {
        if (
            currentWizardStep === DatasourceWizardStep.DATASOURCE_CONFIGURATION
        ) {
            // Already on first step
            return;
        }

        // Determine previous step
        setCurrentWizardStep(
            DatasourceWizardStep[
                DatasourceWizardStep[
                    currentWizardStep - 1
                ] as keyof typeof DatasourceWizardStep
            ]
        );
    };

    const onNext = (): void => {
        if (
            currentWizardStep ===
                DatasourceWizardStep.DATASOURCE_CONFIGURATION &&
            !validateDatasourceConfiguration()
        ) {
            return;
        }

        if (currentWizardStep === DatasourceWizardStep.REVIEW_AND_SUBMIT) {
            // On last step
            props.onFinish && props.onFinish(newDatasource);

            return;
        }

        // Determine next step
        setCurrentWizardStep(
            DatasourceWizardStep[
                DatasourceWizardStep[
                    currentWizardStep + 1
                ] as keyof typeof DatasourceWizardStep
            ]
        );
    };

    const validateDatasourceConfiguration = (): boolean => {
        let validationResult;
        if (
            (validationResult = validateJSON(newDatasourceJSON)) &&
            !validationResult.valid
        ) {
            // Validation failed
            setDatasourceConfigurationError(true);
            setDatasourceConfigurationHelperText(
                validationResult.message || ""
            );

            return false;
        }

        setDatasourceConfigurationError(false);
        setDatasourceConfigurationHelperText("");
        setNewDatasource(JSON.parse(newDatasourceJSON));

        return true;
    };

    const onReset = (): void => {
        const datasource = props.datasource
            ? ({
                  ...props.datasource,
              } as Datasource)
            : createDefaultDatasource();
        setNewDatasource(datasource);
        setNewDatasourceJSON(JSON.stringify(datasource));
    };

    const stepLabelFn = (step: string): string => {
        return t(`label.${kebabCase(DatasourceWizardStep[+step])}`);
    };

    return (
        <>
            {/* Stepper */}
            <Grid container>
                <Grid item sm={12}>
                    <StepperV1
                        activeStep={currentWizardStep.toString()}
                        stepLabelFn={stepLabelFn}
                        steps={Object.values(DatasourceWizardStep).reduce(
                            (steps, datasourceWizardStep) => {
                                if (typeof datasourceWizardStep === "number") {
                                    steps.push(datasourceWizardStep.toString());
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
                                    DatasourceWizardStep[currentWizardStep]
                                )}`
                            )}
                        </Typography>
                    </Grid>

                    {/* Spacer */}
                    <Grid item sm={12} />

                    {/* Datasource configuration */}
                    {currentWizardStep ===
                        DatasourceWizardStep.DATASOURCE_CONFIGURATION && (
                        <>
                            {/* Datasource configuration editor */}
                            <Grid item sm={12}>
                                <JSONEditorV1<Datasource>
                                    error={datasourceConfigurationError}
                                    helperText={
                                        datasourceConfigurationHelperText
                                    }
                                    value={newDatasource}
                                    onChange={onDatasourceConfigurationChange}
                                />
                            </Grid>
                        </>
                    )}

                    {/* Review and submit */}
                    {currentWizardStep ===
                        DatasourceWizardStep.REVIEW_AND_SUBMIT && (
                        <>
                            {/* Datasource information */}
                            <Grid item sm={12}>
                                <JSONEditorV1<Datasource>
                                    readOnly
                                    value={newDatasource}
                                />
                            </Grid>
                        </>
                    )}
                </Grid>

                {/* Controls */}
                <Grid
                    container
                    alignItems="stretch"
                    className={datasourceWizardClasses.controlsContainer}
                    justify="flex-end"
                >
                    {datasourceConfigurationError && (
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
                                        DatasourceWizardStep.DATASOURCE_CONFIGURATION && (
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
                                                DatasourceWizardStep.DATASOURCE_CONFIGURATION
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
                                            DatasourceWizardStep.REVIEW_AND_SUBMIT
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
};
