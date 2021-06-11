import { yupResolver } from "@hookform/resolvers/yup";
import {
    Checkbox,
    FormControl,
    FormControlLabel,
    Grid,
    InputLabel,
    MenuItem,
    Select,
    TextField,
} from "@material-ui/core";
import _ from "lodash";
import React, { FunctionComponent } from "react";
import { Controller, useForm } from "react-hook-form";
import { useTranslation } from "react-i18next";
import * as yup from "yup";
import { LogicalMetric } from "../../../rest/dto/metric.interfaces";
import { createEmptyMetric } from "../../../utils/metrics/metrics.util";
import { useMetricsWizardStyles } from "../metrics-wizard.styles";
import { MetricsPropertiesFormProps } from "./metrics-renderer-form.interfaces";

export const MetricsPropertiesForm: FunctionComponent<MetricsPropertiesFormProps> = (
    props: MetricsPropertiesFormProps
) => {
    const metricWizardClasses = useMetricsWizardStyles();
    const { t } = useTranslation();
    const { register, handleSubmit, errors, control } = useForm<LogicalMetric>({
        defaultValues: props.metric || createEmptyMetric(),
        resolver: yupResolver(
            yup.object().shape({
                name: yup
                    .string()
                    .trim()
                    .required(t("message.metrics-name-required")),
                rollupThreshold: yup
                    .number()
                    .required(t("message.metrics-threshold-required")),
                aggregationFunction: yup
                    .string()
                    .trim()
                    .required(
                        t("message.metrics-aggregation-function-required")
                    ),
                active: yup
                    .boolean()
                    .required(t("message.metrics-active-required")),
            })
        ),
    });

    const onSubmitSusbcriptionGroupPropertiesForm = (
        metric: LogicalMetric
    ): void => {
        props.onSubmit && props.onSubmit(metric);
    };

    return (
        <form
            noValidate
            id={props.id}
            onSubmit={handleSubmit(onSubmitSusbcriptionGroupPropertiesForm)}
        >
            <Grid container>
                {/* Name label */}
                <Grid container item sm={6}>
                    <Grid item sm={8}>
                        {/* Name */}
                        <TextField
                            fullWidth
                            required
                            classes={{
                                root: metricWizardClasses.fieldContainer,
                            }}
                            error={Boolean(errors && errors.name)}
                            helperText={
                                errors && errors.name && errors.name.message
                            }
                            inputRef={register}
                            label={t("label.name")}
                            name="name"
                            type="string"
                            variant="outlined"
                        />
                    </Grid>
                </Grid>
                <Grid container item sm={6}>
                    <Grid item sm={8}>
                        {/* Aggregation Function */}
                        <TextField
                            fullWidth
                            required
                            classes={{
                                root: metricWizardClasses.fieldContainer,
                            }}
                            error={Boolean(
                                errors && errors.aggregationFunction
                            )}
                            helperText={
                                errors &&
                                errors.aggregationFunction &&
                                errors.aggregationFunction.message
                            }
                            inputRef={register}
                            label={t("label.aggregation-function")}
                            name="aggregationFunction"
                            type="string"
                            variant="outlined"
                        />
                    </Grid>
                </Grid>
                <Grid container item sm={6}>
                    <Grid item sm={8}>
                        {/* Dataset */}
                        <FormControl fullWidth variant="outlined">
                            <InputLabel id="metrics-dataset">
                                {t(`label.dataset`)}
                            </InputLabel>
                            <Controller
                                as={
                                    <Select
                                        fullWidth
                                        label={t("label.time-range")}
                                        labelId="metrics-dataset"
                                        variant="outlined"
                                    >
                                        {!_.isEmpty(props.datasets) &&
                                            props.datasets.map(
                                                (dataset, index) => (
                                                    <MenuItem
                                                        key={index}
                                                        value={dataset.name}
                                                    >
                                                        {dataset.name}
                                                    </MenuItem>
                                                )
                                            )}
                                    </Select>
                                }
                                control={control}
                                inputRef={register}
                                name="dataset"
                            />
                        </FormControl>
                    </Grid>
                </Grid>
                <Grid container item sm={6}>
                    <Grid item sm={8}>
                        {/* Threshold */}
                        <TextField
                            fullWidth
                            required
                            classes={{
                                root: metricWizardClasses.fieldContainer,
                            }}
                            error={Boolean(errors && errors.rollupThreshold)}
                            helperText={
                                errors &&
                                errors.rollupThreshold &&
                                errors.rollupThreshold.message
                            }
                            inputRef={register}
                            label={t("label.threshold")}
                            name="rollupThreshold"
                            type="number"
                            variant="outlined"
                        />
                    </Grid>
                </Grid>

                {/* Active label */}
                <Grid container item sm={12}>
                    <Grid item sm={12}>
                        <FormControlLabel
                            control={
                                <Controller
                                    control={control}
                                    inputRef={register}
                                    name="active"
                                    render={({ onChange, value }) => (
                                        <Checkbox
                                            checked={value}
                                            color="primary"
                                            onChange={(e) =>
                                                onChange(e.target.checked)
                                            }
                                        />
                                    )}
                                />
                            }
                            label={t("label.active")}
                        />
                    </Grid>
                </Grid>
            </Grid>
        </form>
    );
};
