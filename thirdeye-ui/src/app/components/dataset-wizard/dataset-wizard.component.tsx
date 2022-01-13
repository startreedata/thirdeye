import { Box, Button, Grid, Typography } from "@material-ui/core";
import { PageContentsCardV1, StepperV1 } from "@startree-ui/platform-ui";
import { kebabCase } from "lodash";
import React, { FunctionComponent, useEffect, useState } from "react";
import { useTranslation } from "react-i18next";
import { Dataset } from "../../rest/dto/dataset.interfaces";
import { createEmptyDataset } from "../../utils/datasets/datasets.util";
import { Dimension } from "../../utils/material-ui/dimension.util";
import { Palette } from "../../utils/material-ui/palette.util";
import { DatasetPropertiesForm } from "../dataset-wizard/dataset-properties-form/dataset-properties-form.component";
import { DatasetRenderer } from "../dataset-wizard/dataset-renderer/dataset-renderer.component";
import {
    DatasetWizardProps,
    DatasetWizardStep,
} from "./dataset-wizard.interfaces";
import { useDatasetWizardStyles } from "./dataset-wizard.styles";

const FORM_ID_DATASET_PROPERTIES = "FORM_ID_DATASET_PROPERTIES";

export const DatasetWizard: FunctionComponent<DatasetWizardProps> = (
    props: DatasetWizardProps
) => {
    const datasetWizardClasses = useDatasetWizardStyles();
    const [newDataset, setNewDataset] = useState<Dataset>(
        props.dataset || createEmptyDataset()
    );
    const [
        currentWizardStep,
        setCurrentWizardStep,
    ] = useState<DatasetWizardStep>(DatasetWizardStep.DATASET_PROPERTIES);
    const { t } = useTranslation();

    useEffect(() => {
        // Notify
        props.onChange && props.onChange(currentWizardStep);
    }, [currentWizardStep]);

    const onSubmitDatasetPropertiesForm = (dataset: Dataset): void => {
        // Update dataset with form inputs
        setNewDataset((newDataset) => Object.assign(newDataset, dataset));

        // Next step
        onNext();
    };

    const onCancel = (): void => {
        props.onCancel && props.onCancel();
    };

    const onBack = (): void => {
        if (currentWizardStep === DatasetWizardStep.DATASET_PROPERTIES) {
            // Already on first step
            return;
        }

        // Determine previous step
        setCurrentWizardStep(
            DatasetWizardStep[
                DatasetWizardStep[
                    currentWizardStep - 1
                ] as keyof typeof DatasetWizardStep
            ]
        );
    };

    const onNext = (): void => {
        if (currentWizardStep === DatasetWizardStep.REVIEW_AND_SUBMIT) {
            // On last step
            props.onFinish && props.onFinish(newDataset);

            return;
        }

        // Determine next step
        setCurrentWizardStep(
            DatasetWizardStep[
                DatasetWizardStep[
                    currentWizardStep + 1
                ] as keyof typeof DatasetWizardStep
            ]
        );
    };

    const stepLabelFn = (step: string): string => {
        return t(`label.${kebabCase(DatasetWizardStep[+step])}`);
    };

    return (
        <>
            {/* Stepper */}
            <Grid container>
                <Grid item sm={12}>
                    <StepperV1
                        activeStep={currentWizardStep.toString()}
                        stepLabelFn={stepLabelFn}
                        steps={Object.values(DatasetWizardStep).reduce(
                            (steps, datasetWizardStep) => {
                                if (typeof datasetWizardStep === "number") {
                                    steps.push(datasetWizardStep.toString());
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
                                    DatasetWizardStep[currentWizardStep]
                                )}`
                            )}
                        </Typography>
                    </Grid>

                    {/* Spacer */}
                    <Grid item sm={12} />

                    {/* Dataset properties */}
                    {currentWizardStep ===
                        DatasetWizardStep.DATASET_PROPERTIES && (
                        <>
                            {/* Dataset properties form */}
                            <Grid item sm={12}>
                                <DatasetPropertiesForm
                                    dataset={newDataset}
                                    datasources={props.datasources}
                                    id={FORM_ID_DATASET_PROPERTIES}
                                    onSubmit={onSubmitDatasetPropertiesForm}
                                />
                            </Grid>

                            {/* Spacer */}
                            <Grid item sm={12} />
                        </>
                    )}

                    {/* Review and submit */}
                    {currentWizardStep ===
                        DatasetWizardStep.REVIEW_AND_SUBMIT && (
                        <>
                            {/* Dataset information */}
                            <DatasetRenderer dataset={newDataset} />
                        </>
                    )}
                </Grid>

                {/* Spacer */}
                <Box padding={2} />

                {/* Controls */}
                <Grid
                    container
                    alignItems="stretch"
                    className={datasetWizardClasses.controlsContainer}
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
                                                DatasetWizardStep.DATASET_PROPERTIES
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
                                        {/* Submit button for dataset properties form in 
                                    first step */}
                                        {currentWizardStep ===
                                            DatasetWizardStep.DATASET_PROPERTIES && (
                                            <Button
                                                color="primary"
                                                form={
                                                    FORM_ID_DATASET_PROPERTIES
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
                                            DatasetWizardStep.DATASET_PROPERTIES && (
                                            <Button
                                                color="primary"
                                                size="large"
                                                variant="contained"
                                                onClick={onNext}
                                            >
                                                {currentWizardStep ===
                                                DatasetWizardStep.REVIEW_AND_SUBMIT
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
